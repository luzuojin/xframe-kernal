package dev.xframe.inject.beans;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dev.xframe.inject.Dependence;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.Providable;
import dev.xframe.inject.code.Codes;
import dev.xframe.utils.XSorter;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

/**
 * 分析依赖关系
 * 循环依赖问题-需要@Inject时显示标注lazy=true
 * 提供加载顺序
 * @author luzj
 */
public class BeanPretreater {

	private List<Class<?>> classes;
    
    private Map<Class<?>, DependenceType> dTypes;
    
    private int index;
    
    private Map<Class<?>, List<Class<?>>> refPrototypes;
    
    public BeanPretreater(List<Class<?>> classes) {
        this.classes = classes;
        
        this.index = 0;
        this.dTypes = new HashMap<>();
        this.refPrototypes = new HashMap<>();
    }
    
    private boolean isProvidable(Class<?> clazz) {
        return clazz.isAnnotationPresent(Providable.class);
    }
    
    public BeanPretreater filter(Predicate<Class<?>> predicate) {
        this.classes = filter0(this.classes, predicate);
        return this;
    }
    
    private List<Class<?>> filter0(List<Class<?>> classes, Predicate<Class<?>> predicate) {
        return classes.stream().filter(predicate).collect(Collectors.toList());
    }
    
    /**
     * 但凡有一个@Providable的接口或者父类已有实现 就表示该类需要被忽略掉
     * @param clazz
     * @return
     */
    private boolean isProvided(Class<?> clazz) {
        if(dTypes.containsKey(clazz)) return true;
        
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interfaze : interfaces) {
            if(isProvidable(interfaze) && isProvided(interfaze)) return true;
        }
        
        Class<?> superClazz = clazz.getSuperclass();
        if(superClazz != null && superClazz != Object.class) {
            if(isProvidable(superClazz) && isProvided(superClazz)) return true;
        }
        return false;
    }
    
    public BeanPretreater pretreat(Comparator<Class<?>> comparator) {
    	return pretreat(comparator, c->false);
    }
    public BeanPretreater pretreat(Comparator<Class<?>> comparator, Predicate<Class<?>> pretrial) {
        List<Class<?>> provides = new ArrayList<>();
        List<Class<?>> analysed = new ArrayList<>();
        
        //首先按字母排序,保证每次加载顺序一样
        XSorter.bubble(classes, (c1, c2) -> c1.getSimpleName().compareTo(c2.getSimpleName()));
        //Annotation排序
        XSorter.bubble(classes, comparator);
        
        for (Class<?> clazz : this.classes) {
            if(isProvidable(clazz)) {
                provides.add(clazz);
            } else {
                analysed.add(clazz);
                putUpwardIfNotPrototype(dTypes, clazz, new DependenceType(clazz));
            }
        }
        
        for (Class<?> provide : provides) {
            if(!isProvided(provide)) {
                analysed.add(provide);
                putUpwardIfNotPrototype(dTypes, provide, new DependenceType(provide));
            }
        }
        
        //过滤掉仅用来帮助分析依赖关系的类
        analysed = filter0(analysed, c->!pretrial.test(c));

        for (Class<?> key : analysed) {
            analyse0(key, new DependenceLink(null, null));
        }

        //按依赖顺序排序
        XSorter.bubble(analysed, (c1, c2) -> Integer.compare(dTypes.get(c1).index, dTypes.get(c2).index));
        
        this.classes = analysed;
        return this;
    }
    
    public List<Class<?>> collect() {
    	return new ArrayList<>(classes);
	}

    public void forEach(Consumer<Class<?>> c) {
        for (Class<?> clazz : classes)
        	c.accept(clazz);
    }
    
    private void analyse0(Class<?> key, DependenceLink parent) {
        parent.checkCircularDependence();
        DependenceType dtype = dTypes.get(key);
        if(dtype == null || dtype.index > 0) return;//已经确定顺序或者不是bean(其他)
        dtype.fields.stream().filter(f->!f.isLazy).forEach(f->analyse0(f.type, new DependenceLink(parent, f.type)));
        Dependence anno = dtype.type.getAnnotation(Dependence.class);
        if(anno != null) Arrays.stream(anno.value()).forEach(c->analyse0(c, new DependenceLink(parent, c)));
        getRefPrototypes(dtype.type).forEach(c->analyse0(c, new DependenceLink(parent, c)));;
        dtype.index = ++index;
    }
    
    public <T> void putUpwardIfNotPrototype(Map<Class<?>, T> map, Class<?> key, T val) {
        putUpward(map, key, val, !key.isAnnotationPresent(Prototype.class));//prototype是通过new Instance来依赖. 所以只需要处理可实例化的类
    }
    
    public <T> void putUpward(Map<Class<?>, T> map, Class<?> key, T val, boolean clazzUpWard) {
        map.putIfAbsent(key, val);
        
        for (Class<?> interfaze : key.getInterfaces()) {
        	putUpward(map, interfaze, val, clazzUpWard);
        }
        
        if(clazzUpWard) {
            Class<?> superClazz = key.getSuperclass();
            if(superClazz != null && superClazz != Object.class)
                putUpward(map, superClazz, val, clazzUpWard);
        }
    }
    //解析Loadable.load实现方法中调用的new @Prototype的类 该类的依赖也需要被优化加载
    private List<Class<?>> getRefPrototypes(Class<?> clazz) {
        if(!refPrototypes.containsKey(clazz)) {
            try {
                ClassPool pool = ClassPool.getDefault();
				CtClass ct = pool.get(clazz.getName());
                
                List<String> refs = new ArrayList<>();
                List<CtBehavior> behaviors = getRefMethods(clazz, ct);
                
                for (CtBehavior behavior : behaviors) {
                    behavior.getDeclaringClass().defrost();
                    behavior.instrument(new ExprEditor(){
                        public void edit(NewExpr e) throws CannotCompileException {
                            if(Codes.isMatching(e.getClassName()))
                                refs.add(e.getClassName());
                        }
                    });
                }
                refPrototypes.put(clazz, classes.stream().filter(c->c.isAnnotationPresent(Prototype.class)&&refs.contains(c.getName())).collect(Collectors.toList()));
            } catch (NotFoundException | CannotCompileException e) {
                //ignore
            }
        }
        return refPrototypes.get(clazz);
    }
    private List<CtBehavior> getRefMethods(Class<?> clazz, CtClass ct) throws NotFoundException {
        List<CtBehavior> behaviors = new ArrayList<>(Arrays.asList(ct.getConstructors()));
        if(Loadable.class.isAssignableFrom(clazz)) {
            behaviors.add(ct.getMethod("load", "()V"));
        }
        return behaviors;
    }
    
    static class DependenceType {
        Class<?> type;
        List<DependenceField> fields = new ArrayList<>();
        int index;
        public DependenceType(Class<?> clazz) {
        	Class<?> t = this.type = clazz;
        	do {
        		for (Field field : t.getDeclaredFields()) {
        			if(field.isAnnotationPresent(Inject.class)) {
        				this.fields.add(new DependenceField(field));
        			}
        		}
        		t = t.getSuperclass();
        	} while(t!=null&&!t.isAnnotationPresent(Prototype.class) && !Object.class.equals(t));
        }
    }
    
    static class DependenceField {
    	Class<?> type;
    	boolean isLazy;
		public DependenceField(Field field) {
			this.type = field.getType();
			this.isLazy = field.getAnnotation(Inject.class).lazy();
		}
    }

    static class DependenceLink {
        public final DependenceLink parent;
        public final Class<?> self;
        public DependenceLink(DependenceLink parent, Class<?> self) {
            this.parent = parent;
            this.self = self;
        }
        public void checkCircularDependence() {
            if(this.self != null) {
                DependenceLink link = this;
                StringBuilder linkStr = new StringBuilder();
                while(link.parent != null && link.parent.self != null) {
                    linkStr.append(link.self.getName()).append(" ");
                    if(this.self.equals(link.parent.self)) {
                    	throw new IllegalArgumentException(String.format("Circular dependence: %s", linkStr.append(this.self.getName())));
                    }
                    link = link.parent;
                }
            }
        }
    }

}

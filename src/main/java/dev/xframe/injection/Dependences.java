package dev.xframe.injection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dev.xframe.injection.Injector.FieldInjector;
import dev.xframe.injection.code.Codes;
import dev.xframe.utils.XSorter;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

class Dependences {
    
    private List<Class<?>> classes;
    
    private Map<Class<?>, Injector> injectors;
    
    private int index;
    
    private Map<Class<?>, List<Class<?>>> refPrototypes;
    
    public Dependences(List<Class<?>> classes) {
        this.classes = classes;
        
        this.index = 0;
        this.injectors = new HashMap<>();
        this.refPrototypes = new HashMap<>();
    }
    
    private boolean isProvidable(Class<?> clazz) {
        return clazz.isAnnotationPresent(Providable.class);
    }
    
    public Dependences filter(Predicate<Class<?>> predicate) {
        this.classes = classes.stream().filter(predicate).collect(Collectors.toList());
        return this;
    }
    
    /**
     * 但凡有一个接口或者父类已有实现 就表示该类需要被忽略掉
     * @param clazz
     * @return
     */
    private boolean isProvided(Class<?> clazz) {
        if(injectors.containsKey(clazz)) return true;
        
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interfaze : interfaces) {
            if(Loadable.class.equals(interfaze)) continue;
            if(isProvided(interfaze)) return true;
        }
        
        Class<?> superClazz = clazz.getSuperclass();
        if(superClazz != null && superClazz != Object.class) {
            if(isProvided(superClazz)) return true;
        }
        return false;
    }
    
    public Dependences analyse() {
        List<Class<?>> provides = new ArrayList<>();
        List<Class<?>> analysed = new ArrayList<>();
        
        for (Class<?> clazz : this.classes) {
            if(isProvidable(clazz)) {
                provides.add(clazz);
            } else {
                analysed.add(clazz);
                putUpwardIfNotPrototype(injectors, clazz, Injection.build(clazz));
            }
        }
        
        for (Class<?> provide : provides) {
            if(!isProvided(provide)) {
                analysed.add(provide);
                putUpwardIfNotPrototype(injectors, provide, Injection.build(provide));
            }
        }
        
        XSorter.bubble(analysed, new Comparator<Class<?>>() {//首先按字母排序,保证每次加载顺序一样
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        XSorter.bubble(analysed, new Comparator<Class<?>>() {//Prototype优先加载
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getAnnotation(Prototype.class) == null && o2.getAnnotation(Prototype.class) != null ? 1 : 0;//o2为Prototype o1不为Prototype, 换位置(o1 > o2)
            }
        });
        
        for (Class<?> key : analysed) {
            analyse0(key, new DependeceLink(null, null));
        }
        
        XSorter.bubble(analysed, new Comparator<Class<?>>() {//把@Bean.prior放置Prototype后
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getAnnotation(Prototype.class) == null && o2.getAnnotation(Prototype.class) != null ? 1 : 0;//o2为Prototype o1不为Prototype, 换位置(o1 > o2)
            }
        });
        XSorter.bubble(analysed, new Comparator<Class<?>>() {//按加载顺序加载
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return (o1.isAnnotationPresent(Prototype.class) ^ o2.isAnnotationPresent(Prototype.class)) ? 0 : injectors.get(o1).index - injectors.get(o2).index;
            }
        });
        
        this.classes = analysed;
        return this;
    }

    public void forEach(Consumer<Class<?>> c) {
        for (Class<?> clazz : classes) c.accept(clazz);
    }

    public Injector getInjector(Class<?> clazz) {
        return injectors.get(clazz);
    }
    
    private void analyse0(Class<?> key, DependeceLink parent) {
        parent.checkCircularDependence();
        Injector injector = injectors.get(key);
        if(injector == null || injector.index > 0) return;//已经确定顺序或者不是bean(其他)
        FieldInjector[] fields = injector.fields;
        Arrays.stream(fields).filter(f->!f.isLazy).forEach(f->analyse0(f.type, new DependeceLink(parent, f.type)));
        Dependence anno = injector.master.getAnnotation(Dependence.class);
        if(anno != null) Arrays.stream(anno.value()).forEach(c->analyse0(c, new DependeceLink(parent, c)));
        getRefPrototypes(injector.master).forEach(c->analyse0(c, new DependeceLink(parent, c)));;
        injector.index = ++index;
    }
    
    public <T> void putUpwardIfNotPrototype(Map<Class<?>, T> map, Class<?> key, T val) {
        putUpward(map, key, val, !key.isAnnotationPresent(Prototype.class));
    }
    
    public <T> void putUpward(Map<Class<?>, T> map, Class<?> key, T val, boolean clazzUpWard) {
        map.put(key, val);
        
        for (Class<?> interfaze : key.getInterfaces())
            putUpward(map, interfaze, val, clazzUpWard);
        
        if(clazzUpWard) {
            Class<?> superClazz = key.getSuperclass();
            if(superClazz != null && superClazz != Object.class)
                putUpward(map, superClazz, val, clazzUpWard);
        }
    }
    
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
                            refs.add(e.getClassName());
                        }
                    });
                }
                refPrototypes.put(clazz, Codes.loadClasses(refs).stream().filter(c->c.isAnnotationPresent(Prototype.class)).collect(Collectors.toList()));
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

    static class DependeceLink {
        public final DependeceLink parent;
        public final Class<?> self;
        public DependeceLink(DependeceLink parent, Class<?> self) {
            this.parent = parent;
            this.self = self;
        }
        public void checkCircularDependence() {
            if(this.self != null) {
                DependeceLink link = this;
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

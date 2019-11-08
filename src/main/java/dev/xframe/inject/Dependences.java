package dev.xframe.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dev.xframe.inject.Injector.FieldInjector;
import dev.xframe.inject.code.Codes;
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
    
    private Map<Class<?>, DependenceInjector> injectors;
    
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
        this.classes = filter(this.classes, predicate);
        return this;
    }
    
    public List<Class<?>> filter(List<Class<?>> classes, Predicate<Class<?>> predicate) {
        return classes.stream().filter(predicate).collect(Collectors.toList());
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
    
    private static Predicate<Class<?>> annotated() {
        return c -> !Modifier.isAbstract(c.getModifiers()) &&
                    !Modifier.isInterface(c.getModifiers()) &&
                    Arrays.stream(annos).filter(a->c.isAnnotationPresent(a)).findAny().isPresent();
    }
    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation>[] annos = new Class[] {Prototype.class, Configurator.class, Repository.class, Templates.class, Bean.class};
    //load order
    private static int annoOrder(Class<?> c) {
        for (int i = 0; i < annos.length; i++) {
            if(c.isAnnotationPresent(annos[i])) {
                return annos.length - i;
            }
        }
        return 0;
    }
    
    public Dependences analyse() {
        filter(annotated());
        
        List<Class<?>> provides = new ArrayList<>();
        List<Class<?>> analysed = new ArrayList<>();
        
        for (Class<?> clazz : this.classes) {
            if(isProvidable(clazz)) {
                provides.add(clazz);
            } else {
                analysed.add(clazz);
                putUpwardIfNotPrototype(injectors, clazz, new DependenceInjector(clazz));
            }
        }
        
        for (Class<?> provide : provides) {
            if(!isProvided(provide)) {
                analysed.add(provide);
                putUpwardIfNotPrototype(injectors, provide, new DependenceInjector(provide));
            }
        }

        //首先按字母排序,保证每次加载顺序一样
        XSorter.bubble(analysed, (c1, c2) -> c1.getSimpleName().compareTo(c2.getSimpleName()));
        //Annotation排序
        XSorter.bubble(analysed, (c1, c2) -> Integer.compare(annoOrder(c2), annoOrder(c1)));
        
        for (Class<?> key : analysed) {
            analyse0(key, new DependenceLink(null, null));
        }
        
        //@Prototype 已经处理过了(@see PrototypePatcher), 仅用来帮助分析依赖关系
        filter(analysed, c->!c.isAnnotationPresent(Prototype.class));
        
        //按依赖顺序排序
        XSorter.bubble(analysed, (c1, c2) -> Integer.compare(injectors.get(c1).index, injectors.get(c2).index));
        
        this.classes = analysed;
        return this;
    }

    public void forEach(Consumer<Class<?>> c) {
        for (Class<?> clazz : classes) c.accept(clazz);
    }

    private void analyse0(Class<?> key, DependenceLink parent) {
        parent.checkCircularDependence();
        DependenceInjector injector = injectors.get(key);
        if(injector == null || injector.index > 0) return;//已经确定顺序或者不是bean(其他)
        FieldInjector[] fields = injector.fields();
        Arrays.stream(fields).filter(f->!f.isLazy).forEach(f->analyse0(f.type, new DependenceLink(parent, f.type)));
        Dependence anno = injector.master().getAnnotation(Dependence.class);
        if(anno != null) Arrays.stream(anno.value()).forEach(c->analyse0(c, new DependenceLink(parent, c)));
        getRefPrototypes(injector.master()).forEach(c->analyse0(c, new DependenceLink(parent, c)));;
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
                            if(Codes.isMatching(e.getClassName()))
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
    
    static class DependenceInjector {
        Injector injector;
        int index;
        public DependenceInjector(Class<?> c) {
            injector = Injection.build(c);
        }
        public Class<?> master() {
            return injector.master;
        }
        public FieldInjector[] fields() {
            return injector.fields;
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

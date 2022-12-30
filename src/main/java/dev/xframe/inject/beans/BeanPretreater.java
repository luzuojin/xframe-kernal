package dev.xframe.inject.beans;

import dev.xframe.inject.Dependence;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.Providable;
import dev.xframe.inject.code.Clazz;
import dev.xframe.inject.code.Codes;
import dev.xframe.inject.code.CtHelper;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 分析依赖关系
 * 循环依赖问题-需要@Inject时显示标注lazy=true
 * 提供加载顺序
 * 依赖的注入项如果在分析是不存在则跳过(动态生成的bean)
 * @author luzj
 */
public class BeanPretreater implements Iterable<Class<?>> {

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

    public BeanPretreater pretreat(Function<Class<?>, Comparable<?>> toComparable) {
        return pretreat(toComparable, c->false);
    }
    public BeanPretreater pretreat(Function<Class<?>, Comparable<?>> toComparable, Predicate<Class<?>> pretrial) {
        //首先按字母排序,保证每次处理顺序一样
        makeOrderly(classes, Class::getSimpleName);
        //Annotation排序
        makeOrderly(classes, toComparable);

        List<Class<?>> provides = new ArrayList<>();
        List<Class<?>> analysed = new ArrayList<>();

        //处理@Providable
        for (Class<?> clazz : this.classes) {
            if(clazz.isAnnotation()) {
                continue;//annotaion 不处理
            }
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

        //由于@Providable的处理打乱了Classes的顺序, 重新排序
        makeOrderly(analysed, Class::getSimpleName);
        makeOrderly(analysed, toComparable);

        //过滤掉仅用来帮助分析依赖关系的类
        analysed = filter0(analysed, c->!pretrial.test(c));

        for (Class<?> key : analysed) {
            analyse0(key, new DependenceLink(null, null));
        }

        //按依赖顺序排序
        makeOrderly(analysed, c->dTypes.get(c).index);

        this.classes = analysed;
        return this;
    }

    public List<Class<?>> collect() {
        return new ArrayList<>(classes);
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return classes.iterator();
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
                ClassPool pool = CtHelper.getClassPool();
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
        @Override
        public String toString() {
            return "DependenceType [type=" + type + ", index=" + index + "]";
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

    public static class Annotated {
        final Class<? extends Annotation>[] annotateds;
        @SafeVarargs
        public Annotated(Class<? extends Annotation>... annotateds) {
            this.annotateds = annotateds;
        }
        public int getPriority(Class<?> c) {
            for (int i = 0; i < annotateds.length; i++) {
                if(c.isAnnotationPresent(annotateds[i])) {
                    return i;
                }
            }
            return annotateds.length;
        }
        public boolean isPresient(Class<?> c) {
            return Arrays.stream(annotateds).anyMatch(c::isAnnotationPresent);
        }
        public boolean isPresient(Clazz c) {
            return Arrays.stream(annotateds).anyMatch(c::isAnnotationPresent);
        }
        public Function<Class<?>, Comparable<?>> comparator() {
            return this::getPriority;
        }
    }
    public static class Composited {
        final Class<? extends Annotation> composited;
        public Composited(Class<? extends Annotation> composited) {
            this.composited = composited;
        }
        public int getPriority(Class<?> c) {
            return Arrays.stream(c.getInterfaces()).anyMatch(i->i.isAnnotationPresent(composited)) ? -1 : 1;
        }
        public Function<Class<?>, Comparable<?>> comparator() {
            return this::getPriority;
        }
    }

    public static Function<Class<?>, Comparable<?>> comparator(Annotated annotated, Class<? extends Annotation> composited) {
        return comparator(annotated, new Composited(composited));
    }
    public static Function<Class<?>, Comparable<?>> comparator(Annotated annotated, Composited composited) {
        return c -> 10 * annotated.getPriority(c) + composited.getPriority(c);
    }

    public static <T> List<T> makeOrderly(List<T> list, Function<T, Comparable<?>> toComparable) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Function<T, Comparable<Object>> toC = (Function<T, Comparable<Object>>) (Function) toComparable;
        int len = list.size();
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < len - i - 1; j++) {
                if (toC.apply(list.get(j)).compareTo(toC.apply(list.get(j + 1))) > 0) {
                    list.set(j, list.set(j + 1, list.get(j)));
                }
            }
        }
        return list;
    }

}

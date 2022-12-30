package dev.xframe.inject.code;

import dev.xframe.utils.XCaught;
import javassist.CtClass;
import javassist.NotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * for class lazy load
 */
public class Clazz {
    private final CtClass cc;
    public Clazz(CtClass cc) {
        this.cc = cc;
    }
    public static Clazz of(CtClass cc) {
        return new Clazz(cc);
    }

    private Class<?> cached;
    public Class<?> toClass() {
        try {
            if(cached == null)
                cached = Class.forName(cc.getName());
            return cached;
        } catch (ClassNotFoundException e) {
            throw XCaught.throwException(e);
        }
    }

    public boolean isPrimitive() {
        return cc.isPrimitive();
    }
    public boolean isInterface() {
        return cc.isInterface();
    }
    public boolean isAnnotation() {
        return cc.isAnnotation();
    }
    public boolean isEnum() {
        return cc.isEnum();
    }
    public boolean isArray() {
        return cc.isArray();
    }

    public int getModifiers() {
        return cc.getModifiers();
    }
    public String getName() {
        return cc.getName();
    }
    public String getSimpleName() {
        return cc.getSimpleName();
    }
    public String getPackageName() {
        return cc.getPackageName();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotated) {
        return cc.hasAnnotation(annotated);
    }
    public <A extends Annotation> A getAnnotation(Class<A> annotated) {
        try {
            return (A) cc.getAnnotation(annotated);
        } catch (ClassNotFoundException e) {
            throw XCaught.throwException(e);
        }
    }
    public Object[] getAnnotations() {
        try {
            return cc.getAnnotations();
        } catch (ClassNotFoundException e) {
            throw XCaught.throwException(e);
        }
    }
    public Clazz getSuperclass() {
        try {
            return Clazz.of(cc.getSuperclass());
        } catch (NotFoundException e) {
            throw XCaught.throwException(e);
        }
    }
    public Clazz[] getInterfaces() {
        try {
            return Arrays.stream(cc.getInterfaces()).map(Clazz::of).toArray(Clazz[]::new);
        } catch (NotFoundException e) {
            throw XCaught.throwException(e);
        }
    }

    /**
     * is Object.class
     * @return
     */
    public boolean isObject() {
        return isClass(Object.class);
    }
    public boolean isClass(Class<?> cls) {
        return isClass(cc, cls);
    }

    /**
     * not (Interface & Abstract)
     * @return
     */
    public boolean isImplementation() {
        return !(Modifier.isAbstract(cc.getModifiers()) || cc.isInterface());
    }

    public boolean isImplementedFrom(Class<?> cls) {
        return isImplementedFrom(cc, cls);
    }
    private static boolean isImplementedFrom(CtClass cc, Class<?> cls) {
        try {
            if(isClass(cc, Object.class)) {
                return false;
            }
            if(isClass(cc, cls)) {
                return true;
            }
            if(Arrays.stream(cc.getInterfaces()).anyMatch(ci->isImplementedFrom(ci, cls))) {
                return true;
            }
            return isImplementedFrom(cc.getSuperclass(), cls);
        } catch (NotFoundException e) {
            //ignore;
        }
        return false;
    }
    private static boolean isClass(CtClass cc, Class<?> cls) {
        return cc.getName().equals(cls.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public static List<Class<?>> filter(List<Clazz> clzList, Predicate<Clazz> predicate) {
        return clzList.stream().filter(predicate).map(Clazz::toClass).collect(Collectors.toList());
    }

    @SafeVarargs
    public static Predicate<Clazz> filter(Predicate<Clazz>... predicates) {
        Predicate<Clazz> predicate = c -> true;
        for (Predicate<Clazz> p : predicates) {
            predicate = predicate.and(p);
        }
        return predicate;
    }
    @SafeVarargs
    public static Predicate<Clazz> filter(Class<? extends Annotation>... cs) {
        return clz->Arrays.stream(cs).anyMatch(clz::isAnnotationPresent);
    }

}

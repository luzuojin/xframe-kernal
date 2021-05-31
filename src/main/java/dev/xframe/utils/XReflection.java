package dev.xframe.utils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javassist.Modifier;

@SuppressWarnings("unchecked")
public class XReflection extends SecurityManager {

    static final XReflection relection = new XReflection();
    @Override
    protected Class<?>[] getClassContext() {
        return super.getClassContext();
    }

    public static Class<?> getCallerClass() {
        return getCallerClass(2);
    }

    public static Class<?> getCallerClass(int depth) {
        Class<?>[] classes = relection.getClassContext();
        int index = depth + 2;
        int len = classes.length;
        return len > index ? classes[index] : classes[len - 1];
    }

    /**
     * @param clazz
     * @param name
     * @param parameterTypes
     * @return declared method
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method res = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name) && Arrays.equals(parameterTypes, m.getParameterTypes()) && (res == null || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }
        if(res != null)
            XReflection.setAccessible(res);
        return res;
    }
    public static <T> T invoke(Method method, Object obj, Object... args) {
        try {
            return (T) method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            return XCaught.throwException(e.getCause());
        } catch (Exception e) {
            return XCaught.throwException(e);
        }
    }

    /**
     * @param clazz
     * @param name
     * @return declared field
     */
    public static Field getField(Class<?> clazz, String name) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                XReflection.setAccessible(field);
                return field;
            }
        }
        return null;
    }
    public static void invoke(Field field, Object obj, Object val) {
        try {
            field.set(obj, val);
        } catch (Exception e) {
            XCaught.throwException(e);
        }
    }
    public static <T> T invoke(Field field, Object obj) {
        try {
            return (T) field.get(obj);
        } catch (Exception e) {
            return XCaught.throwException(e);
        }
    }

    /**
     * @param <T>
     * @param clazz
     * @param parameterTypes
     * @return declared constructor
     */
    public static <T> Constructor<T> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if(Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                XReflection.setAccessible(constructor);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }
    public static <T> T invoke(Constructor<?> constructor, Object... args) {
        try {
            return (T) constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            return XCaught.throwException(e.getCause());
        } catch (Exception e) {
            return XCaught.throwException(e);
        }
    }
    public static <T> T newInstance(Class<?> clazz) {
        return invoke(getConstructor(clazz));
    }


    /**
     * 获取Class.isAssignableFrom为true的所有类
     */
    public static List<Class<?>> getAssigners(Class<?> clazz) {
        return getAssigners0(new ArrayList<>(), clazz);
    }

    private static List<Class<?>> getAssigners0(List<Class<?>> assigners, Class<?> clazz) {
        assigners.add(clazz);
        Class<?> zuper = clazz.getSuperclass();
        if(zuper != null && !Object.class.equals(zuper))
            getAssigners0(assigners, zuper);
        for (Class<?> interfaze : clazz.getInterfaces())
            getAssigners0(assigners, interfaze);
        return assigners;
    }

    /**
     * 是否是实现类(可实例化, 非Abstract 非Interface)
     * @param c
     */
    public static boolean isImplementation(Class<?> c) {
        return !(Modifier.isAbstract(c.getModifiers()) || c.isInterface());
    }

    static Consumer<AccessibleObject> AccessibleSetter;
    static {
        try {
            double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));
            if(JAVA_VERSION < 9) {
                AccessibleSetter = XReflection::setAccessibleSimply;
            } else {
                //jdk9(JPMS) 非add-opens模块不能直接调用Accessible.setAccessible, 可以使用Unsafe修改override属性
                //jdk12 AccessibleObject.override在反射列表(Fields)中被屏蔽了.(Unsafe修改属性不再可行)
                final MethodHandle setter = XLookup.lookup().findSetter(AccessibleObject.class, "override", boolean.class);
                AccessibleSetter = ao -> {
                    try {
                        setter.invokeWithArguments(ao, Boolean.TRUE);
                    } catch (Throwable e) {
                        XCaught.throwException(e);
                    }
                };
            }
        } catch (Throwable e) {
            //ignore
        }
    }
    private static void setAccessibleSimply(AccessibleObject ao) {
        if (System.getSecurityManager() == null) {
            ao.setAccessible(true);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    ao.setAccessible(true);
                    return null;
                }
            });
        }
    }

    /**
     * AccessibleObject.setAccessible(true)
     */
    public static void setAccessible(AccessibleObject ao) {
        AccessibleSetter.accept(ao);
    }

}
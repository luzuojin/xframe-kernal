package dev.xframe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
     * get resource in classpath or as file
     * @param res
     * @return
     */
    public static InputStream getResourceAsStream(String res) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(res);
        if(input == null) {
            File f = new File(res);
            if(f.exists()) {
                try {
                    input = new FileInputStream(f);
                } catch (FileNotFoundException e) {}//ignore
            }
        }
        return input;
    }

    /**
     * @param cls
     * @param predicate
     */
    public static List<Method> getMethods(Class<?> cls, Predicate<Method> predicate) {
        return getMethods0(cls, predicate, new ArrayList<>());
    }
    private static List<Method> getMethods0(Class<?> cls, Predicate<Method> predicate, List<Method> dest) {
        if(cls == null) {
            return dest;
        }
        for (Method m : cls.getDeclaredMethods()) {
            if (predicate.test(m)) {
                XReflection.setAccessible(m);
                dest.add(m);
            }
        }
        return getMethods0(cls.getSuperclass(), predicate, dest);
    }

    /**
     * @param cls
     * @param name
     * @param parameterTypes
     * @return method
     */
    public static Method getMethod(Class<?> cls, String name, Class<?>... parameterTypes) {
        if(cls == null) {
            return null;
        }
        Method res = null;
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name) && Arrays.equals(parameterTypes, m.getParameterTypes()) && (res == null || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }
        if(res != null) {
            XReflection.setAccessible(res);
            return res;
        }
        return getMethod(cls.getSuperclass(), name, parameterTypes);
    }
    public static <T> T invokeMethod(Method method, Object obj, Object... args) {
        try {
            return (T) method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw XCaught.throwException(e.getCause());
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    public static <T> T invokeStaticMethod(Method method, Object... args) {
        return invokeMethod(method, null, args);
    }

    /**
     * @param cls
     * @param predicate
     * @return fields
     */
    public static List<Field> getFields(Class<?> cls, Predicate<Field> predicate) {
        return getFields0(cls, predicate, new ArrayList<>());
    }
    private static List<Field> getFields0(Class<?> cls, Predicate<Field> predicate, List<Field> dest) {
        if(cls == null) {
            return dest;
        }
        for (Field field : cls.getDeclaredFields()) {
            if (predicate.test(field)) {
                XReflection.setAccessible(field);
                dest.add(field);
            }
        }
        return getFields0(cls.getSuperclass(), predicate, dest);
    }
    
    /**
     * @param cls
     * @param name
     * @return declared field
     */
    public static Field getField(Class<?> cls, String name) {
        if(cls == null) {
            return null;
        }
        for (Field field : cls.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                XReflection.setAccessible(field);
                return field;
            }
        }
        return getField(cls.getSuperclass(), name);
    }
    public static void invokeSetter(Field field, Object obj, Object val) {
        try {
            field.set(obj, val);
        } catch (Exception e) {
            XCaught.throwException(e);
        }
    }
    public static void invokeStaticSetter(Field field, Object val) {
        invokeSetter(field, null, val);
    }
    public static <T> T invokeGetter(Field field, Object obj) {
        try {
            return (T) field.get(obj);
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    public static <T> T invokeStaticGetter(Field field) {
        return invokeGetter(field, null);
    }

    /**
     * @param clazz
     * @param parameterTypes
     * @return declared constructor
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if(Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                XReflection.setAccessible(constructor);
                return constructor;
            }
        }
        return null;
    }
    public static <T> T invokeConstructor(Constructor<?> constructor, Object... args) {
        try {
            return (T) constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw XCaught.throwException(e.getCause());
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    
    public static <T> T newInstance(Class<?> clazz) {
        return invokeConstructor(getConstructor(clazz));
    }

    public static MethodHandle findGetter(Class<?> refc, String name, Class<?> type) {
        try {
            return XLookup.lookup().findGetter(refc, name, type);
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    public static MethodHandle findSetter(Class<?> refc, String name, Class<?> type) {
        try {
            return XLookup.lookup().findSetter(refc, name, type);
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
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
    
    /*
     * get lambda method ref
     */
    private static Method poolObjGetter;
    private static Method poolSizeGetter;
    private static Method poolMethodGetter;
    static {
        try {
            poolObjGetter = XReflection.getMethod(Class.class, "getConstantPool");
            Class<?> poolClass = poolObjGetter.invoke(Class.class).getClass();
            poolSizeGetter = XReflection.getMethod(poolClass, "getSize");
            poolMethodGetter = XReflection.getMethod(poolClass, "getMethodAt", int.class);
        } catch (Exception e) {e.printStackTrace();}//ignore
    }
    public static Method getLambdaFuncMethod(Class<?> lambdaType) {
        Class<?> parentType = lambdaType.getInterfaces()[0];
        for (Method method : parentType.getMethods()) {
            if(!method.isDefault() && !Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        }
        return null;
    }
    public static Member getLambdaRefMember(Class<?> lambdaType) {
        try {
            Object pool = poolObjGetter.invoke(lambdaType);
            int size = (Integer) poolSizeGetter.invoke(pool);
            for (int i = 0; i < size; i++) {
                Member member = poolMethodAt(pool, i);
                if(member == null || isLambdaInternal(member, lambdaType)) {
                    continue;
                }
                if(!(member instanceof Method && XBoxing.isBoxingMethod((Method) member))) {
                    return member;
                }
            }
            return null;
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }
    private static boolean isLambdaInternal(Member member, Class<?> lambdaType) {
        Class<?> declaringClass = member.getDeclaringClass();
        return declaringClass == lambdaType || (member instanceof Constructor && (declaringClass == Object.class || declaringClass == SerializedLambda.class));
    }
    private static Member poolMethodAt(Object pool, int i) {
        try {
            return (Member) poolMethodGetter.invoke(pool, i);
        } catch (Exception e) {}//ignore
        return null;
    }

}
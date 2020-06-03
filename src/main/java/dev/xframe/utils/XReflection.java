package dev.xframe.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            res.setAccessible(true);
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
                field.setAccessible(true);
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
                constructor.setAccessible(true);
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
    
    /**
     * 获取Class.isAssignableFrom为true的所有类
     */
    public static List<Class<?>> getAssigners(Class<?> clazz) {
    	List<Class<?>> parents = new ArrayList<>();
    	getAssigners0(parents, clazz);
    	return parents;
    }

	private static void getAssigners0(List<Class<?>> assigners, Class<?> clazz) {
		assigners.add(clazz);
		Class<?> zuper = clazz.getSuperclass();
		if(zuper != null && !Object.class.equals(zuper))
			getAssigners0(assigners, zuper);
		for (Class<?> interfaze : clazz.getInterfaces())
			getAssigners0(assigners, interfaze);
	}
	
	/**
	 * 是否是实现类(可实例化, 非Abstract 非Interface)
	 * @param c
	 */
	public static boolean isImplementation(Class<?> c) {
	    return !(Modifier.isAbstract(c.getModifiers()) || c.isInterface());
	}
    
}
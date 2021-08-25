package dev.xframe.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class XBoxing {

    private static final Map<Class<?>, Class<?>> primitiveMap = new HashMap<>();
    private static final Map<Class<?>, Class<?>>   wrapperMap = new HashMap<>();
    private static void put(Class<?> primitive, Class<?> wrapper) {
        primitiveMap.put(primitive, wrapper);
          wrapperMap.put(wrapper, primitive);
    }
    static {
        put(boolean.class, Boolean.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(short.class, Short.class);
        put(int.class, Integer.class);
        put(float.class, Float.class);
        put(long.class, Long.class);
        put(double.class, Double.class);
    }
    
    public static Class<?> getPrimitive(Class<?> wrapper) {
        return wrapper.isPrimitive() ? wrapper : wrapperMap.get(wrapper);
    }
    public static Class<?> getWrapper(Class<?> primitive) {
        return primitive.isPrimitive() ? primitiveMap.get(primitive) : primitive;
    }
    
    //jdk generated autobox method
    //boxing: Integer.valueOf
    //unboxing: Integer.intValue
    public static boolean isBoxingMethod(Method method) {
        if(method.getReturnType().isPrimitive()) {//Integer.intValue
            if(method.getParameterTypes().length != 0) {
                return false;
            }
            return isBoxingMethod(method, method.getReturnType(), method.getDeclaringClass());
        } else {//Integer.valueOf
            if(method.getParameterTypes().length != 1) {
                return false;
            }
            return isBoxingMethod(method, method.getParameterTypes()[0], method.getReturnType());
        }
    }
    private static boolean isBoxingMethod(Method method, Class<?> primitive, Class<?> wrapper) {
        return primitive.isPrimitive() && (!wrapper.isPrimitive())
                && primitive == getPrimitive(wrapper)
                && wrapper == getWrapper(primitive)
                && method.getDeclaringClass() == wrapper;
    }
    
    //for code generation
    public static boolean unbox(Boolean b) {
        return b.booleanValue();
    }
    public static byte unbox(Byte b) {
        return b.byteValue();
    }
    public static char unbox(Character c) {
        return c.charValue();
    }
    public static short unbox(Short s) {
        return s.shortValue();
    }
    public static int unbox(Integer i) {
        return i.intValue();
    }
    public static float unbox(Float f) {
        return f.floatValue();
    }
    public static long unbox(Long l) {
        return l.longValue();
    }
    public static double unbox(Double d) {
        return d.doubleValue();
    }
    public static Object unbox(Object o) {
        return o;
    }
    
    public static Boolean box(boolean b) {
        return Boolean.valueOf(b);
    }
    public static Byte box(byte b) {
        return Byte.valueOf(b);
    }
    public static Character box(char c) {
        return Character.valueOf(c);
    }
    public static Short box(short s) {
        return Short.valueOf(s);
    }
    public static Integer box(int i) {
        return Integer.valueOf(i);
    }
    public static Float box(float f) {
        return Float.valueOf(f);
    }
    public static Long box(long l) {
        return Long.valueOf(l);
    }
    public static Double box(double d) {
        return Double.valueOf(d);
    }
    public static Object box(Object o) {
        return o;
    }
}

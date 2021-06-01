package dev.xframe.utils;

import java.lang.reflect.Constructor;

@FunctionalInterface
public interface XFactory<T> {
    
    public T get();
    
    @SuppressWarnings("unchecked")
    public static  <T> XFactory<T> of(Class<T> cls) {
        if(cls.isPrimitive()) {
            throw new IllegalArgumentException("Unsupported type: " + cls);
        }
        Constructor<?>[] cons = cls.getDeclaredConstructors();
        for (Constructor<?> c : cons) {
            if(c.getParameterCount() == 0) {//has default constructor
                return XLambda.createByConstructor(XFactory.class, cls);
            }
        }
        XLogger.debug("Create factory for [{}] without default constructor", cls);
        //don`t exec init code
        return () -> (T) XUnsafe.allocateInstance(cls);
    }
    
}

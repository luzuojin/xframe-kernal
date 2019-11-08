package dev.xframe.inject.code;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

import javax.annotation.Resource;

import dev.xframe.inject.Injector;

public class InjectionCode {
    static final String FIELD_NAME = "_injector";
    //static final Injector _injector = Injection.build(x.class);
    static final String FIELD_TEMP = "static final %s %s = %s.%s(%s.class);";
    //Injection.inject(%s, _injector, ...);
    static final String CALL_TEMP = "%s.%s(%s);";
    
    final Class<?> injectionClass;
    
    public InjectionCode(Class<?> injectionClass) {
        this.injectionClass = injectionClass;
    }
    
    public String field(String declaringClass) {
        return String.format(FIELD_TEMP, Injector.class.getName(), FIELD_NAME, injectionClass.getName(), findMarkMethod(injectionClass, Injector.class), declaringClass);
    }

    public String call(String injectArgs) {
        return String.format(CALL_TEMP, injectionClass.getName(), findMarkMethod(injectionClass, Object.class), String.format(injectArgs, FIELD_NAME));
    }
    
    
    public static String findMarkMethod(Class<?> clazz, Class<?> returnType) {
        return findMarkMethod(clazz, m->m.getReturnType().equals(returnType));
    }
    public static String findMarkMethod(Class<?> clazz, Predicate<Method> predicate) {
        return Arrays.stream(clazz.getMethods()).filter(m->m.isAnnotationPresent(Resource.class)).filter(predicate).map(Method::getName).findAny().orElse(null);
    }
    
}
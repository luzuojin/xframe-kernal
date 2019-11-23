package dev.xframe.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.inject.Injector.FieldInjector;
import dev.xframe.utils.XCaught;

public class Injection {
    
    private static final Logger logger = LoggerFactory.getLogger(Injection.class);
    
    private static Map<Class<?>, Injector> injectors = new HashMap<>();
    
    private static BeanContainer defbc = new BeanContainer();

    static List<Class<?>> injectedClasses(Predicate<Class<?>> predicate) {
        return injectors.keySet().stream().filter(predicate).collect(Collectors.toList());
    }
    
    public static Injector get(Class<?> clazz) {
        return injectors.get(clazz);
    }
    public static Injector build(Class<?> clazz) {
        return build(clazz, Injector::build);
    }
    public static Injector build(Class<?> clazz, Function<Field, FieldInjector> builder) {
        Injector injector = Injector.build(clazz, builder);
        injectors.put(clazz, injector);
        return injector;
    }
    public static Injector getOrbuild(Class<?> clazz) {
        Injector injector = get(clazz);
        return injector == null ? build(clazz) : injector;
    }
    
    public static Object inject(Object bean) {
        return inject(bean, defbc);
    }
    public static Object inject(Object bean, BeanContainer bc) {
        return inject(bean, getOrbuild(bean.getClass()), bc);
    }
    public static Object makeInstanceAndInject(Class<?> clazz) {
        return makeInstanceAndInject(clazz, defbc);
    }
    public static Object makeInstanceAndInject(Class<?> clazz, BeanContainer bc) {
        return inject(makeInstance(clazz), getOrbuild(clazz), bc);
    }
    
    public static Object makeInstance(Class<?> clazz) {
        try {
            Constructor<?> con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            Object bean = con.newInstance();
            return bean;
        } catch (Throwable e) {
            throw XCaught.wrapException(clazz.getName(), e);
        }
    }

    public static Object inject(Object bean, Injector injector) {
        return inject(bean, injector, defbc);
    }
    public static Object inject(Object bean, Injector injector, BeanContainer bc) {
        injector.inject(bean, bc);
        load(bean);
        return bean;
    }
    
    static void load(Object bean) {
        if(bean instanceof Loadable) {
            long start = System.currentTimeMillis();
            ((Loadable) bean).load();
            long used = System.currentTimeMillis() - start;
            logger.info("Load completed {} used {}ms", bean.getClass().getName(), used);
        }
    }
    
    public static class BeanContainer {
        public Object get(Class<?> clazz) {
            return ApplicationContext.fetchBeanTryProxy(clazz);
        };
        public Object get(String name) {
            return ApplicationContext.fetchBean(name);
        };
    }

}

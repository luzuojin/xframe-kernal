package dev.xframe.inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import dev.xframe.inject.code.Codes;
import dev.xframe.inject.code.Factory;
import dev.xframe.inject.code.FactoryBuilder;
import dev.xframe.inject.code.ProxyBuilder;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.inject.code.ProxyBuilder.IProxy;

public class ApplicationContext {
    
    private static Map<Class<?>, Object> beanClassMap = new HashMap<Class<?>, Object>();
    private static Map<String, Object> beanNameMap = new HashMap<String, Object>();
    
    @SuppressWarnings("unchecked")
    public static <T> T fetchBean(String name) {
        return (T) beanNameMap.get(name);
    }
    @SuppressWarnings("unchecked")
    public static <T> T fetchBean(Class<T> clazz) {
        return (T) beanClassMap.get(clazz);
    }
    
    public static void registBean(String name, Object bean) {//通过名字区别
        beanNameMap.put(name, bean);
    }
    public static void registBean(Object bean) {//通过类型区别
        registBean(bean.getClass(), bean);
    }
    public static void registBean(Class<?> clazz, Object bean) {
        registBean0(clazz, makeProxy(clazz, bean));
    }

    protected static void registBean0(Class<?> clazz, Object bean) {
        beanClassMap.putIfAbsent(clazz, makeSynthetic(clazz, bean));
        
        Class<?> superclazz = clazz.getSuperclass();
        if(superclazz != null && superclazz != Object.class)
            registBean(superclazz, bean);
        
        for(Class<?> interfaze : clazz.getInterfaces())
            registBean(interfaze, bean);
    }

    protected static boolean isSyntheticRequired(Class<?> clazz) {
        return clazz.isAnnotationPresent(Synthetic.class);
    }
    protected static Object makeSynthetic(Class<?> clazz, Object bean) {
        if(isSyntheticRequired(clazz)) {
            Object exists = beanClassMap.get(clazz);
            if(exists == null) {
                exists = SyntheticBuilder.buildBean(clazz);
            }
            if(bean != null) {
                SyntheticBuilder.append(exists, bean);
            }
            return exists;
        }
        return bean;
    }

    protected static boolean isProxyRequired(Class<?> clazz) {
        return clazz.isAnnotationPresent(Templates.class) || (clazz.isAnnotationPresent(Bean.class) && clazz.getAnnotation(Bean.class).reloadable());
    }
    protected static Map<Class<?>, Object> proxies = new HashMap<>();
    protected static Object makeProxy(Class<?> clazz, Object bean) {
        if(!isProxyRequired(clazz)) return bean;
        
        Object proxy = proxies.get(clazz);
        if(proxy == null) {
            proxy = ProxyBuilder.build(clazz);
            proxies.put(clazz, proxy);
        }
        if(bean != null) {
            ProxyBuilder.setDelegate(proxy, bean);
        }
        return proxy;
    }

    protected static Object fetchBeanTryProxy(Class<?> clazz) {
        if(fetchBean(clazz) == null && (isProxyRequired(clazz) || isSyntheticRequired(clazz))) {//Bean不存在 如果可以为该bean创建Proxy
            registBean(clazz, null);
        }
        return fetchBean(clazz);
    }

    public static void initialize(String includes, String excludes) {
        registBean(Eventual.class, null);     //empty Eventual bean
        initialize(Codes.getClasses(includes, excludes));
        fetchBean(Eventual.class).eventuate();//execute Eventuals
    }

    private static void initialize(List<Class<?>> classes) {
        loadFactories(classes);
        //singleton beans And Prototypes(prototype set injector)
        loadBeans(classes);
    }
    
    private static void loadFactories(List<Class<?>> classes) {
        classes.stream().filter(c -> c.isInterface() && c.isAnnotationPresent(Factory.class)).forEach(c -> registBean(c, FactoryBuilder.build(c, classes)));
    }
    
    private static void loadBeans(List<Class<?>> classes) {
        new Dependences(classes).analyse().forEach(c -> registBean(Injection.makeInstanceAndInject(c)));
    }
    
    /**
     * 重新执行load(替换delegate, 线程安全)
     */
    public static void reload(Class<?> clazz) {
        Object obj = fetchBean(clazz);
        if(obj != null && obj instanceof IProxy) {
            Object delegate = ProxyBuilder.getDelegate(obj);
            if(delegate instanceof IProxy) {
                ProxyBuilder.setDelegate(delegate, Injection.makeInstanceAndInject(ProxyBuilder.getDelegate(delegate).getClass()));
            } else {
                ProxyBuilder.setDelegate(obj, Injection.makeInstanceAndInject(delegate.getClass()));
            }
        }
    }
    
    public static void reload(Predicate<Class<?>> preficate) {
    	Injection.injectedClasses(preficate).forEach(ApplicationContext::reload);
    }
    
    /**
     * 使用新的class文件替换原有class
     */
    public static void replace(Class<?> clazz, Class<?> newClazz) {
        Object obj = fetchBean(clazz);
        if(obj != null && obj instanceof IProxy) {
            ProxyBuilder.setDelegate(obj, ProxyBuilder.build(clazz, Injection.makeInstanceAndInject(newClazz)));
        }
    }
    
}
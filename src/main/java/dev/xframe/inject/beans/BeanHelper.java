package dev.xframe.inject.beans;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.code.CompositeBuilder.IComposite;
import dev.xframe.inject.code.ProxyBuilder.IProxy;
import dev.xframe.utils.XReflection;

import java.util.function.Consumer;

/**
 * 
 * global bean helper
 * @author luzj
 *
 */
public class BeanHelper {
	
	//for generated composite bean
    public static void removeComposite(Object composite, Object delegate) {
        ((IComposite) composite)._removeDelegate(delegate);
    }
    public static void appendComposite(Object composite, Object delegate) {
        ((IComposite) composite)._appendDelegate(delegate);
    }
    public static void forEachComposite(Object composite, Consumer<?> consumer) {
        ((IComposite) composite)._forEachDeletage(consumer);
    }
    //for generated Proxy
    public static boolean isProxy(Object bean) {
        return bean instanceof IProxy;
    }
    public static void setProxyDelegate(Object bean, Object delegate) {
        ((IProxy) bean)._setDelegate(delegate);
    }
    public static Object getProxyDelegate(Object bean) {
        return ((IProxy) bean)._getDelegate();
    }

    //for injection
	public static Injector makeInjector(Class<?> c) {
		return Injector.of(c, ApplicationContext.Internal.indexing(), !c.isAnnotationPresent(Prototype.class));//prototype父类会有一个独立的Injector
	}
	public static <T> T inject(Class<T> c) {
		return inject(XReflection.newInstance(c), makeInjector(c));
	}
	public static <T> T inject(T bean) {
		return inject(bean, makeInjector(bean.getClass()));
	}
	public static <T> T inject(T bean, Injector injector) {
		injector.inject(bean, ApplicationContext.Internal.fetcher());
		BeanLoader.doLoad(bean);
		return bean;
	}

}

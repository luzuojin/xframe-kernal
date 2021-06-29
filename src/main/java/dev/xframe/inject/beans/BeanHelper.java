package dev.xframe.inject.beans;

import java.util.function.Consumer;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.code.CompositeBuilder.IComposite;
import dev.xframe.inject.code.ProxyBuilder.IProxy;
import dev.xframe.utils.XReflection;

/**
 * 
 * global bean helper
 * @author luzj
 *
 */
public class BeanHelper {
	
	//for generated Synthetic
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
	public static Object inject(Class<?> c) {
		return inject(XReflection.newInstance(c), makeInjector(c));
	}
	public static Object inject(Object bean) {
		return inject(bean, makeInjector(bean.getClass()));
	}
	public static Object inject(Object bean, Injector injector) {
		injector.inject(bean, ApplicationContext.Internal.fetcher());
		Loadable.doLoad(bean);
		return bean;
	}

}

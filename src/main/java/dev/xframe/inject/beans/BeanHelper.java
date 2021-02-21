package dev.xframe.inject.beans;

import java.util.function.Consumer;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.code.ProxyBuilder.IProxy;
import dev.xframe.inject.code.SyntheticBuilder.ISynthetic;
import dev.xframe.utils.XReflection;

/**
 * 
 * global bean helper
 * @author luzj
 *
 */
public class BeanHelper {
	
	//for generated Synthetic
    public static void removeSynthetic(Object synthetic, Object delegate) {
        ((ISynthetic) synthetic)._removeDelegate(delegate);
    }
    public static void appendSynthetic(Object synthetic, Object delegate) {
        ((ISynthetic) synthetic)._appendDelegate(delegate);
    }
    public static void forEachSynthetic(Object synthetic, Consumer<?> consumer) {
        ((ISynthetic) synthetic)._forEachDeletage(consumer);
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
		return Injector.of(c, ApplicationContext.indexing(), !c.isAnnotationPresent(Prototype.class));//prototype父类会有一个独立的Injector
	}
	public static Object inject(Class<?> c) {
		return inject(XReflection.newInstance(c), makeInjector(c));
	}
	public static Object inject(Object bean) {
		return inject(bean, makeInjector(bean.getClass()));
	}
	public static Object inject(Object bean, Injector injector) {
		injector.inject(bean, ApplicationContext.definer());
		Loadable.doLoad(bean);
		return bean;
	}

}

package dev.xframe.inject.beans;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Prototype;
import dev.xframe.utils.XCaught;

public class BeanHelper {
	
	private static Object newInstance(Class<?> c) {
		try {
			return c.newInstance();
		} catch (Exception e) {
			return XCaught.throwException(e);
		}
	}
    
	public static Injector build(Class<?> c) {
		return Injector.of(c, ApplicationContext.indexing(), !c.isAnnotationPresent(Prototype.class));//prototype父类会有一个独立的Injector
	}
	public static Object inject(Class<?> c) {
		return inject(newInstance(c), build(c));
	}
	public static Object inject(Object bean) {
		return inject(bean, build(bean.getClass()));
	}
	public static Object inject(Object bean, Injector injector) {
		injector.inject(bean, ApplicationContext.definer());
		Loadable.doLoad(bean);
		return bean;
	}

}

package dev.xframe.inject.beans;

import dev.xframe.inject.ApplicationContext;
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
		return Injector.of(c, ApplicationContext.indexing());
	}
	public static Object inject(Class<?> c) {
		return inject(newInstance(c), build(c));
	}
	public static Object inject(Object bean) {
		return inject(bean, build(bean.getClass()));
	}
	public static Object inject(Object bean, Injector injector) {
		injector.inject(bean, ApplicationContext.definer());
		return bean;
	}

}

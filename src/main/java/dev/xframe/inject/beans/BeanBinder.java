package dev.xframe.inject.beans;

import java.util.Arrays;
import java.util.List;

import dev.xframe.inject.Loadable;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;

/**
 * 记录Bean的索引值
 * 实例化Bean
 * 完成Bean加载逻辑
 * 输出Bean的可注入Keyword(interfaces...)
 * 解决同一个Parent有多个实现时的冲突问题
 * @author luzj
 */
public abstract class BeanBinder {
	
	int index = -1;
	
	public BeanBinder() {
	}
	
	public int getIndex() {
		return index;
	}
	
	protected abstract List<?> getKeywords();
	protected abstract Object newInstance();
	//完成注入以及加载的过程
	protected abstract void integrate(Object bean, BeanDefiner definer);
	//当某接口/父类有多个binder映射时
	protected abstract BeanBinder conflict(Object keyword, BeanBinder binder);
	
	public static BeanBinder classic(Class<?> clazz, Injector injector) {
		return new Classic(clazz, injector);
	}
	//bean已经初始化完成
	public static BeanBinder named(String key, Object bean) {
		return new Named(key, bean);
	}
	//bean已经初始化完成
	public static BeanBinder instanced(Object bean, Class<?>... keys) {
		return new Instanced(bean, keys);
	}
	
	public static class Classic extends BeanBinder {
		protected final Class<?> master;
		protected final Injector injector;
		public Classic(Class<?> master, Injector injector) {
			this.master = master;
			this.injector = injector;
		}
		protected void integrate(Object bean, BeanDefiner definer) {
			injector.inject(bean, definer);
			Loadable.doLoad(bean);
		}
		protected Object newInstance() {
			return newInstance0(master);
		}
		protected List<?> getKeywords() {
			return XReflection.getAssigners(master);
		}
		protected BeanBinder conflict(Object keyword, BeanBinder binder) {
			return this;
		}
		protected Object newInstance0(Class<?> c) {
			try {
				return c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				return XCaught.throwException(e);
			}
		}
	}
	
	public static class Instanced extends BeanBinder {
		protected final Object val;
		protected final Class<?>[] keys;
		public Instanced(Object val, Class<?>... keys) {
			this.val = val;
			this.keys = keys;
		}
		protected void integrate(Object bean, BeanDefiner definer) {
			//do nothing now
		}
		protected Object newInstance() {
			return val;
		}
		protected List<?> getKeywords() {
			return Arrays.asList(keys);
		}
		protected BeanBinder conflict(Object keyword, BeanBinder binder) {
			return this;
		}
	}
	public static class Named extends BeanBinder {
		protected final String key;
		protected final Object val;
		public Named(String key, Object val) {
			this.key = key;
			this.val = val;
		}
		protected void integrate(Object bean, BeanDefiner definer) {
			//do nothing now
		}
		protected Object newInstance() {
			return val;
		}
		protected List<?> getKeywords() {
			return Arrays.asList(key);
		}
		protected BeanBinder conflict(Object keyword, BeanBinder binder) {
			throw new IllegalArgumentException("Exists bean named[" + key + "]");
		}
	}

}

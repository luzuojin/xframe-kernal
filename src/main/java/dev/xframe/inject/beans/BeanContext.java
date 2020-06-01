package dev.xframe.inject.beans;

import java.util.Arrays;
import java.util.stream.Stream;

public class BeanContext extends BeanContainer implements BeanRegistrator, BeanProvider, BeanIndexing {
	
	public BeanContext() {
		super(new BeanIndexes());
		this.regist(BeanBinder.instanced(this, getInterfaces()));
	}
	
	@Override
	public int indexOf(Object keyword) {
		return indexes.getIndex(keyword);
	}

	private Class<?>[] getInterfaces() {
		return Stream.concat(Arrays.stream(BeanContext.class.getInterfaces()), Arrays.stream(BeanContainer.class.getInterfaces())).toArray(Class[]::new);
	}
	
	@Override
	public <T> T getBean(Class<T> clazz) {
		return (T) getBean(indexOf(clazz));
	}
	@Override
	public <T> T getBean(String name) {
		return (T) getBean(indexOf(name));
	}
	//仅设置到indexes里边
	public void registBinder(BeanBinder binder) {
		if(binder.index == -1) {
			indexes.regist(binder);
		}
	}
	@Override
	public synchronized void regist(BeanBinder binder) {
		if(binder.index == -1) {//registed
			this.registBinder(binder);
			this.integrate(binder);
		}
	}
	public BeanBinder getBinder(Object keyword) {
		int index = indexes.getIndex(keyword);
		return index == -1 ? null : indexes.getBinder(index);
	}
}

package dev.xframe.inject.beans;

public interface BeanProvider {
	
	<T> T getBean(Class<T> clazz);

	<T> T getBean(String name);
	
}

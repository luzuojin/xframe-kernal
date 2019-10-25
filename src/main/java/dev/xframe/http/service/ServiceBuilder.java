package dev.xframe.http.service;

import dev.xframe.injection.Bean;
import dev.xframe.injection.Injection;
import dev.xframe.injection.Providable;

@Bean
@Providable
public class ServiceBuilder {
	
	public String findPath(Class<?> clazz) {
		if(clazz.isAnnotationPresent(Http.class)) {
			return clazz.getAnnotation(Http.class).value();
		}
		return null;
	}

    public Service build(Class<?> clazz) {
    	return (Service) newOrigin(clazz);
    }

	protected Object newOrigin(Class<?> clazz) {
		return Injection.makeInstanceAndInject(clazz);
	}
    
}

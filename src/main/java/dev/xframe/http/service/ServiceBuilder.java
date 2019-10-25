package dev.xframe.http.service;

import dev.xframe.injection.Bean;
import dev.xframe.injection.Injection;
import dev.xframe.injection.Providable;

@Bean
@Providable
public class ServiceBuilder {
	
    public Service build(Class<?> clazz) {
    	return (Service) newOrigin(clazz);
    }

	protected Object newOrigin(Class<?> clazz) {
		return Injection.makeInstanceAndInject(clazz);
	}
    
}

package dev.xframe.http.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.xframe.inject.Configurator;
import dev.xframe.inject.beans.BeanHelper;

@Configurator
public class ServiceBuilder {
    
    private Map<Predicate<Class<?>>, Function<Class<?>, Service>> builders = new HashMap<>(4);

    public void regist(Predicate<Class<?>> predicate, Function<Class<?>, Service> builder) {
        builders.put(predicate, builder);
    }
	
    public Service build(Class<?> clazz) {
        for (Map.Entry<Predicate<Class<?>>, Function<Class<?>, Service>> entry : builders.entrySet()) {
            if(entry.getKey().test(clazz)) {
                return entry.getValue().apply(clazz);
            }
        }
    	return build0(clazz);
    }

	private Service build0(Class<?> clazz) {
		return (Service) BeanHelper.inject(clazz);
	}
    
}

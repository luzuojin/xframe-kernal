package dev.xframe.http.service.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dev.xframe.http.service.Rest;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.ServiceBuilder;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.config.ServiceConfig;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.injection.Loadable;
import dev.xframe.utils.XStrings;

@Bean
public class RestServiceBuilder implements Loadable {
	
	@Inject
	private ServiceConfig config;
	@Inject
	private ServiceBuilder sbuilder;
	@Inject(lazy=true)
	private ServiceContext serviceCtx;
	
	@Override
    public void load() {
	    sbuilder.regist(c->c.isAnnotationPresent(Rest.class), this::build);
    }

	public Service build(Class<?> clazz) {
		Object origin = Injection.makeInstanceAndInject(clazz);
		
		Method mres = null;
        for (Method m : findMethods(clazz)) {
            if(XStrings.isEmpty(findSubPath(m))) {
                mres = m;
                continue;
            }
            serviceCtx.registService(findPath(clazz, m), buildService(origin, m));
        }
        return buildService(origin, mres);
	}

	private Service buildService(Object origin, Method method) {
		return buildRestInvoker(buildRestAdapter(origin, method));
	}
	
	private RestServiceInvoker buildRestInvoker(RestService service) {
		return new RestServiceInvoker(service, config.getRespEncoder());
	}
	
	private RestService buildRestAdapter(Object origin, Method method) {
		return RestServiceAdapter.of(method, origin, config.getBodyDecoder());
	}

    private String findPath(Class<?> clazz, Method m) {
    	return XStrings.trim(Service.findPath(clazz), '/') + '/' + XStrings.trim(findSubPath(m), '/');
    }
    
	private String findSubPath(Method m) {
		if(m.isAnnotationPresent(HttpMethods.GET.class)) {
            return m.getAnnotation(HttpMethods.GET.class).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.POST.class)) {
	        return m.getAnnotation(HttpMethods.POST.class).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.PUT.class)) {
	        return m.getAnnotation(HttpMethods.PUT.class).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.DELETE.class)) {
	        return m.getAnnotation(HttpMethods.DELETE.class).value();
	    }
	    if(m.isAnnotationPresent(HttpMethods.OPTIONS.class)) {
	        return m.getAnnotation(HttpMethods.OPTIONS.class).value();
	    }
	    return null;
	}
	
	private List<Method> findMethods(Class<?> clazz) {
		List<Method> r = new ArrayList<>();
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
		    String p = findSubPath(m);
		    if(p != null) {
		        r.add(m);
		    }
		}
		return r;
	}

}

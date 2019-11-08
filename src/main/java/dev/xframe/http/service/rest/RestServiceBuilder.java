package dev.xframe.http.service.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.xframe.http.service.Rest;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.ServiceBuilder;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.config.ServiceConfig;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Injection;
import dev.xframe.inject.Loadable;
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
		
		List<Method> mres = Collections.emptyList();
        for (Map.Entry<String, List<Method>> e : findMethods(clazz).entrySet()) {
            if(XStrings.isEmpty(e.getKey())) {
                mres = e.getValue();
                continue;
            }
            serviceCtx.registService(toFullPath(clazz, e.getKey()), buildService(origin, e.getValue()));
        }
        return buildService(origin, mres);
	}

	private Service buildService(Object origin, List<Method> methods) {
		return buildRestInvoker(buildRestAdapter(origin, methods));
	}
	
	private RestServiceInvoker buildRestInvoker(RestService service) {
		return new RestServiceInvoker(service, config.getRespEncoder());
	}
	
	private RestService buildRestAdapter(Object origin, List<Method> methods) {
		return RestServiceAdapter.of(methods, origin, config.getBodyDecoder());
	}

    private String toFullPath(Class<?> clazz, String sub) {
    	return XStrings.trim(Service.findPath(clazz), '/') + '/' + sub;
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
	
	private Map<String, List<Method>> findMethods(Class<?> clazz) {
		Map<String, List<Method>> r = new HashMap<>();
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
		    String p = findSubPath(m);
		    if(p != null) {
		        addTo(r, XStrings.trim(p, '/'), m);
		    }
		}
		return r;
	}

    private void addTo(Map<String, List<Method>> r, String k, Method m) {
        List<Method> v = r.get(k);
        if(v == null) {
            v = new ArrayList<>();
            r.put(k, v);
        }
        v.add(m);
    }

}

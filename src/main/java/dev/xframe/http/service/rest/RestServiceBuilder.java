package dev.xframe.http.service.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.xframe.http.service.Rest;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.ServiceBuilder;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.config.ServiceConfig;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Inject;
import dev.xframe.utils.XStrings;

@Bean
public class RestServiceBuilder extends ServiceBuilder {
	
	@Inject
	private ServiceConfig config;
	@Inject(lazy=true)
	private ServiceContext serviceCtx;
	
	@Override
    public Service build(Class<?> clazz) {
        return clazz.isAnnotationPresent(Rest.class) ? build4Rest(clazz) : super.build(clazz);
    }

	public Service build4Rest(Class<?> clazz) {
		Object origin = newOrigin(clazz);
		buildSubServices(clazz, origin);
		return buildService(origin, findMethods(clazz, false));
	}

	private void buildSubServices(Class<?> clazz, Object origin) {
        Arrays.stream(findMethods(clazz, true)).forEach(m->serviceCtx.registService(findPath(clazz, m), buildService(origin, m)));
	}

	private Service buildService(Object origin, Method... methods) {
		return buildRestInvoker(buildRestAdapter(origin, methods));
	}
	
	private RestServiceInvoker buildRestInvoker(RestService service) {
		return new RestServiceInvoker(service, config.getRespEncoder());
	}
	
	private RestService buildRestAdapter(Object origin, Method... methods) {
		Method m = methods.length == 1 ? methods[0] : null;
		return RestServiceAdapter.of(m, origin, config.getBodyDecoder());
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
	
	private Method[] findMethods(Class<?> clazz, boolean sub) {
		List<Method> r = new ArrayList<>();
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
		    String p = findSubPath(m);
		    if(p != null && (XStrings.isEmpty(p) != sub)) {
		        r.add(m);
		    }
		}
		return r.toArray(new Method[0]);
	}

}

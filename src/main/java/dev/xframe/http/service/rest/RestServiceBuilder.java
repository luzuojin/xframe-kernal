package dev.xframe.http.service.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.http.service.Http;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.ServiceBuilder;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.tools.XStrings;

@Bean
public class RestServiceBuilder implements ServiceBuilder {
	
	@Inject
	private RestConfig config;
	@Inject(lazy=true)
	private ServiceContext serviceContext;
	
    public void setConfiguration(RestConfig config) {
        this.config = config;
    }

    public void setServiceContext(ServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}

	@Override
    public Service build(Class<?> clazz) throws Exception {
        if(RestService.class.isAssignableFrom(clazz)) {
            return build4Rest(clazz);
        }
        return (Service) clazz.newInstance();
    }

	public Service build4Rest(Class<?> clazz) {
		RestService service = (RestService) Injection.makeInstanceAndInject(clazz);
		buildSubResServices(clazz, service);
		return buildInvoker(buildAdapter(service, findMethods(clazz, false)));
	}

	private void buildSubResServices(Class<?> clazz, RestService service) {
        Arrays.stream(findMethods(clazz, true)).collect(Collectors.groupingBy(cm->findSubRes(clazz, cm))).forEach((k, v) -> {
            //check repeated http methods
            serviceContext.registService(k, buildInvoker(buildAdapter(service, v.toArray(new Method[0]))));
        });
	}

    private String findSubRes(Class<?> clazz, Method m) {
    	return XStrings.trim(clazz.getAnnotation(Http.class).value(), '/') + '/' + XStrings.trim(findMethodRes(m), '/');
    }

	private RestServiceInvoker buildInvoker(RestService service) {
		return new RestServiceInvoker(service, config.getRespEncoder());
	}
	
	private RestService buildAdapter(RestService origin, Method[] methods) {
		Method m = methods.length == 1 ? methods[0] : null;
		return RestServiceAdapter.of(m, origin, config.getBodyDecoder());
	}

	private Method[] findMethods(Class<?> clazz, boolean subRes) {
		List<Method> r = new ArrayList<>();
		Method[] ms = clazz.getDeclaredMethods();
		for (Method m : ms) {
		    String res = findMethodRes(m);
		    if(res != null && (XStrings.isEmpty(res) != subRes)) {
		        r.add(m);
		    }
		}
		return r.toArray(new Method[0]);
	}
	
	private String findMethodRes(Method m) {
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
	

}

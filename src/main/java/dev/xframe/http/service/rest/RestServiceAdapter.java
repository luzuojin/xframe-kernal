package dev.xframe.http.service.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.config.BodyDecoder;
import dev.xframe.http.service.path.PathMatcher;

public class RestServiceAdapter implements RestService  {
	
	final Object delegate;
	final Invoker[] invokers;
	
	RestServiceAdapter(Object delegate, Invoker[] invokers) {
	    this.delegate = delegate;
	    this.invokers = invokers;
    }

    public static RestService of(List<Method> method, Object delegate, BodyDecoder decoder) {
		return new RestServiceAdapter(delegate, newInvokers(method, decoder));
	}
	
	private static ArgParser[] newParsers(Parameter[] parameters, BodyDecoder bodyDecoder) {
		return Arrays.stream(parameters).map(p->ArgParsers.of(p, bodyDecoder)).toArray(l->new ArgParser[l]);
	}

    private static Invoker[] newInvokers(List<Method> methods, BodyDecoder decoder) {
        Invoker[] invokers = new Invoker[5];
        for (Method method : methods) {
            if(method.isAnnotationPresent(HttpMethods.GET.class)) invokers[0] = new Invoker(method, decoder);
            if(method.isAnnotationPresent(HttpMethods.PUT.class)) invokers[1] = new Invoker(method, decoder);
            if(method.isAnnotationPresent(HttpMethods.POST.class)) invokers[2] = new Invoker(method, decoder);
            if(method.isAnnotationPresent(HttpMethods.DELETE.class)) invokers[3] = new Invoker(method, decoder);
            if(method.isAnnotationPresent(HttpMethods.OPTIONS.class)) invokers[4] = new Invoker(method, decoder);
        }
        return invokers;
    }
    
    @Override
    public Object get(Request req, PathMatcher matcher) throws Throwable {
        if(invokers[0] != null) {
            return invokers[0].doInvoke(delegate, req, matcher);
        }
        return RestService.super.get(req, matcher);
    }

    @Override
    public Object put(Request req, PathMatcher matcher) throws Throwable {
        if(invokers[1] != null) {
            return invokers[1].doInvoke(delegate, req, matcher);
        }
        return RestService.super.put(req, matcher);
    }
    
    @Override
    public Object post(Request req, PathMatcher matcher) throws Throwable {
        if(invokers[2] != null) {
            return invokers[2].doInvoke(delegate, req, matcher);
        }
        return RestService.super.post(req, matcher);
    }

    @Override
    public Object delete(Request req, PathMatcher matcher) throws Throwable {
        if(invokers[3] != null) {
            return invokers[3].doInvoke(delegate, req, matcher);
        }
        return RestService.super.delete(req, matcher);
    }

    @Override
    public Object options(Request req, PathMatcher matcher) throws Throwable {
        if(invokers[4] != null) {
            return invokers[4].doInvoke(delegate, req, matcher);
        }
        return RestService.super.options(req, matcher);
    }
    
    static class Invoker {
        Method method;
        ArgParser[] parsers;
        Invoker(Method method, BodyDecoder decoder) {
            this.method = method;
            this.method.setAccessible(true);
            this.parsers = newParsers(method.getParameters(), decoder);
        }
        Object doInvoke(Object delegate, Request req, PathMatcher matcher) throws Throwable {
            try {
                Object[] args = new Object[parsers.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = parsers[i].parse(req, matcher);
                }
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

}

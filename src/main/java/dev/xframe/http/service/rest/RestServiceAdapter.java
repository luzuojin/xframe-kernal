package dev.xframe.http.service.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.config.BodyDecoder;
import dev.xframe.http.service.path.PathMatcher;

public class RestServiceAdapter implements RestService  {
	
	protected Object delegate;
	protected Method method;
	protected ArgParser[] parsers;
	
	protected final Object doInvoke(Request req, PathMatcher matcher) {
		try {
			Object[] args = new Object[parsers.length];
			for (int i = 0; i < args.length; i++) {
				args[i] = parsers[i].parse(req, matcher);
			}
			return method.invoke(delegate, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static RestService of(Method method, Object delegate, BodyDecoder decoder) {
		return newAdapter(method).complete(method, delegate, decoder);
	}
	
	protected RestService complete(Method method, Object delegate, BodyDecoder b) {
		this.delegate = delegate;
		this.method = method;
		this.method.setAccessible(true);
		this.parsers = newParsers(method.getParameters(), b);
		return this;
	}

	private ArgParser[] newParsers(Parameter[] parameters, BodyDecoder bodyDecoder) {
		return Arrays.stream(parameters).map(p->ArgParsers.of(p, bodyDecoder)).toArray(l->new ArgParser[l]);
	}

	private static RestServiceAdapter newAdapter(Method method) {
		if(method != null) {
			for (Annotation anno : method.getAnnotations()) {
				if(anno instanceof HttpMethods.GET) return new GetAdapter();
				if(anno instanceof HttpMethods.PUT) return new PutAdapter();
				if(anno instanceof HttpMethods.POST) return new PostAdapter();
				if(anno instanceof HttpMethods.DELETE) return new DeleteAdapter();
				if(anno instanceof HttpMethods.OPTIONS) return new OptionsAdapter();
			}
		}
		return new NilAdapter();
	}

	public static class NilAdapter extends RestServiceAdapter {
		protected RestService complete(Method m, Object o, BodyDecoder b) {
			return this;
		}
	}
	public static class GetAdapter extends RestServiceAdapter {
		public Object get(Request req, PathMatcher matcher) {
			return doInvoke(req, matcher);
		}
	}
	public static class PutAdapter extends RestServiceAdapter {
		public Object put(Request req, PathMatcher matcher) {
			return doInvoke(req, matcher);
		}
	}
	public static class PostAdapter extends RestServiceAdapter {
		public Object post(Request req, PathMatcher matcher) {
			return doInvoke(req, matcher);
		}
	}
	public static class DeleteAdapter extends RestServiceAdapter {
		public Object delete(Request req, PathMatcher matcher) {
			return doInvoke(req, matcher);
		}
	}
	public static class OptionsAdapter extends RestServiceAdapter {
		public Object options(Request req, PathMatcher matcher) {
			return doInvoke(req, matcher);
		}
	}

}

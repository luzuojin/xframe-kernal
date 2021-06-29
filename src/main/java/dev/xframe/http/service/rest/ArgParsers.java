package dev.xframe.http.service.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import dev.xframe.http.Request;
import dev.xframe.http.config.BodyDecoder;
import dev.xframe.http.service.path.PathMatcher;
import dev.xframe.utils.XStrings;

public class ArgParsers {
	
	/**
	 * body之外的参数均由string转换
	 */
	static final Map<Class<?>, Function<String, Object>> simpleParsers = new HashMap<>();
	static {
		offer(int.class, Integer::parseInt);
		offer(Integer.class, Integer::parseInt);
		offer(long.class, Long::parseLong);
		offer(Long.class, Long::parseLong);
		offer(boolean.class, Boolean::parseBoolean);
		offer(Boolean.class, Boolean::parseBoolean);
		offer(String.class, s->s);
	}
	static Function<String, Object> getParser(Class<?> type) {
		Function<String, Object> func = simpleParsers.get(type);
		if(func == null)
			throw new IllegalArgumentException(String.format("Unsupported rest arg type[%s], set parser by ArgParsers.offer", type.getName()));
		return func;
	}
	
	public static void offer(Class<?> c, Function<String, Object> parser) {
		simpleParsers.put(c, parser);
	}
	
	public static ArgParser of(Parameter p, BodyDecoder b) {
		CompletableArgParser ap = newArgParser(p);
		ap.complete(p, b);
		return ap;
	}
	
	static CompletableArgParser newArgParser(Parameter p) {
		if(Request.class.isAssignableFrom(p.getType())) {
			return new ReqParser();
		}
		for (Annotation anno : p.getAnnotations()) {
			if(anno instanceof HttpArgs.Body) return new BodyParser();
			if(anno instanceof HttpArgs.Path) return new PathParser();
			if(anno instanceof HttpArgs.Param) return new ParamParser();
			if(anno instanceof HttpArgs.Header) return new HeaderParser();
		}
		throw new IllegalArgumentException("not support argument type [" + p.getType().getName() + ":" + p.getName() + "]");
	}

	static interface CompletableArgParser extends ArgParser {
		default void complete(Parameter p, BodyDecoder b) {
		}
	}
	static class ReqParser implements CompletableArgParser {
		public Object parse(Request req, PathMatcher matcher) {
			return req;
		}
	}
	static class BodyParser implements CompletableArgParser {
		Class<?> type;
		BodyDecoder decoder;
		public void complete(Parameter p, BodyDecoder b) {
			type = p.getType();
			decoder = b;
		}
		public Object parse(Request req, PathMatcher matcher) {
			return decoder.decode(type, req.body());
		}
	}
	static class ParamParser implements CompletableArgParser {
		Function<String, Object> parser;
		String key;
		public void complete(Parameter p, BodyDecoder b) {
			key = XStrings.orElse(p.getAnnotation(HttpArgs.Param.class).value(), p.getName());
			parser = getParser(p.getType());
		}
		public Object parse(Request req, PathMatcher matcher) {
			return parser.apply(req.getParam(key));
		}
	}
	static class PathParser implements CompletableArgParser {
		Function<String, Object> parser;
		String key;
		public void complete(Parameter p, BodyDecoder b) {
			key = XStrings.orElse(p.getAnnotation(HttpArgs.Path.class).value(), p.getName());
			parser = getParser(p.getType());
		}
		public Object parse(Request req, PathMatcher matcher) {
			return parser.apply(matcher.group(key));
		}
	}
	static class HeaderParser implements CompletableArgParser {
		Function<String, Object> parser;
		String key;
		public void complete(Parameter p, BodyDecoder b) {
			key = XStrings.orElse(p.getAnnotation(HttpArgs.Header.class).value(), p.getName());
			parser = getParser(p.getType());
		}
		public Object parse(Request req, PathMatcher matcher) {
			return parser.apply(req.getHeader(key));
		}
	}

}

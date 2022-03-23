package dev.xframe.http.service.rest;

import dev.xframe.http.Request;
import dev.xframe.http.config.BodyDecoder;
import dev.xframe.http.service.path.PathMatcher;
import dev.xframe.utils.XDateFormatter;
import dev.xframe.utils.XStrings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ArgParsers {
	
	/**
	 * body之外的参数均由string转换
	 */
	static final Map<Class<?>, Function<String, ?>> TypedParsers = new HashMap<>();
	static {
		offer(boolean.class, Boolean::parseBoolean);
		offer(int.class, Integer::parseInt);
		offer(float.class, Float::parseFloat);
		offer(long.class, Long::parseLong);
		offer(double.class, Double::parseDouble);
		offer(Integer.class, Integer::parseInt);
		offer(Long.class, Long::parseLong);
		offer(Float.class, Float::parseFloat);
		offer(Boolean.class, Boolean::parseBoolean);
		offer(Double.class, Double::parseDouble);
		offer(String.class, Function.identity());
		offer(LocalDate.class, XDateFormatter::toLocalDate);
		offer(LocalTime.class, XDateFormatter::toLocalTime);
		offer(Date.class, XDateFormatter::toDate);
		offer(Timestamp.class, XDateFormatter::toTimestamp);
		offer(LocalDateTime.class, XDateFormatter::toLocalDateTime);
	}
	static Function<String, ?> getParser(Class<?> type) {
		Function<String, ?> func = TypedParsers.get(type);
		if(func == null)
			throw new IllegalArgumentException(String.format("Unsupported rest arg type[%s], set parser by HttpConfigSetter or ArgParsers.offer", type.getName()));
		return func;
	}
	
	public static <T> void offer(Class<T> c, Function<String, T> parser) {
		TypedParsers.put(c, c.isPrimitive() ? primitiveParser(parser) : objectParser(parser));
	}
	private static <T> Function<String, T> primitiveParser(Function<String, T> parser) {
		return v -> parser.apply(XStrings.orElse(v, "0"));//default: 0/false
	}
	private static <T> Function<String, Object> objectParser(Function<String, T> parser) {
		return v -> XStrings.isEmpty(v) ? null : parser.apply(v);//default: null
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
		Function<String, ?> parser;
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
		Function<String, ?> parser;
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
		Function<String, ?> parser;
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

package dev.xframe.http.service.rest;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.Response;
import dev.xframe.http.service.path.PathMatcher;

public interface RestService {
	
	//impelemention by DynamicBuilder
	public default Object get(Request req, PathMatcher matcher) {
		return new IllegalArgumentException("unsupported method [get]");
	}
	
	public default Object post(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("unsupported method [post]");
	}
	
	public default Object put(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("unsupported method [put]");
	}
	
	public default Object delete(Request req, PathMatcher matcher) {
	    return new IllegalArgumentException("unsupported method [delete]");
	}
	
	public default Object options(Request req, PathMatcher matcher) {
		return Response.OPTIONS_DEFAULT.retain();
	}

}

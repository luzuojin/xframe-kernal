package dev.xframe.http.service.rest;

import dev.xframe.http.Request;
import dev.xframe.http.response.Responses;
import dev.xframe.http.service.path.PathMatcher;

public interface RestService {
	
	//impelemention by DynamicBuilder
	public default Object get(Request req, PathMatcher matcher) throws Throwable {
		return new IllegalArgumentException("unsupported method [get]");
	}
	
	public default Object post(Request req, PathMatcher matcher) throws Throwable {
	    return new IllegalArgumentException("unsupported method [post]");
	}
	
	public default Object put(Request req, PathMatcher matcher) throws Throwable {
	    return new IllegalArgumentException("unsupported method [put]");
	}
	
	public default Object delete(Request req, PathMatcher matcher) throws Throwable {
	    return new IllegalArgumentException("unsupported method [delete]");
	}
	
	public default Object options(Request req, PathMatcher matcher) throws Throwable {
		return Responses.OPTIONS_DEF;
	}

}

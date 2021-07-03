package dev.xframe.http.service.rest;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.service.path.PathMatcher;

public interface RestService {
	
	//impelemention by RestServiceAdapter
	public default Object get(Request req, PathMatcher matcher) {
		return new UnsupportedOperationException("Unsupported method [GET]");
	}
	
	public default Object post(Request req, PathMatcher matcher) {
	    return new UnsupportedOperationException("Unsupported method [POST]");
	}
	
	public default Object put(Request req, PathMatcher matcher) {
	    return new UnsupportedOperationException("Unsupported method [PUT]");
	}
	
	public default Object delete(Request req, PathMatcher matcher) {
	    return new UnsupportedOperationException("Unsupported method [DELETE]");
	}
	
	public default Object options(Request req, PathMatcher matcher) {
		return Response.EMPTY;
	}

}

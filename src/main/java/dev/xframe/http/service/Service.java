package dev.xframe.http.service;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.service.path.PathMatcher;

/**
 * 
 * http service method interface
 * @author luzj
 * 
 */
public interface Service {
	
	default Response exec(Request req, PathMatcher matcher) throws Throwable {
		return exec(req);
	}
    
    /**
     * @param req
     * @return resp (just support String)
     */
    public Response exec(Request req) throws Throwable;
    
    
    public static String findPath(Class<?> c) {
    	if(c.isAnnotationPresent(Http.class)) {
    		return c.getAnnotation(Http.class).value();
    	}
    	if(c.isAnnotationPresent(Rest.class)) {
    		return c.getAnnotation(Rest.class).value();
    	}
    	return null;
    }
    
}

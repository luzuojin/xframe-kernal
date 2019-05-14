package dev.xframe.http.service;

import dev.xframe.http.service.uri.PathMatcher;

/**
 * 
 * http service method interface
 * @author luzj
 * 
 */
public interface Service {
	
	public default Response service(Request req, PathMatcher matcher) {
		return service(req);
	}
    
    /**
     * @param req
     * @return resp (just support String)
     */
    public Response service(Request req);

}

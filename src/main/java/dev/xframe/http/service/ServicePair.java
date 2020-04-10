package dev.xframe.http.service;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.service.path.PathMatcher;

public class ServicePair {
    
    final PathMatcher matcher;
    final Service service;
    
    ServicePair(PathMatcher matcher, Service service) {
        this.matcher = matcher;
        this.service = service;
    }

	public Response invoke(Request req) throws Exception {
		return service.exec(req, matcher);
	}
    
}

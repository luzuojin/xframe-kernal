package dev.xframe.http.service;

import dev.xframe.http.service.config.HttpInterceptor;
import dev.xframe.http.service.path.PathMatcher;

public class ServiceInvoker {
    
    final HttpInterceptor interceptor;
    final PathMatcher matcher;
    final Service service;
    
    ServiceInvoker(HttpInterceptor interceptor, PathMatcher matcher, Service service) {
        this.interceptor = interceptor;
        this.matcher = matcher;
        this.service = service;
    }
    
    public Response invoke(Request req) throws Throwable {
        if(interceptor == null) {
            return service.service(req, matcher);
        }
        //interceptor
        Response resp = interceptor.before(req);
        if(resp == null) {
            resp = service.service(req, matcher);
        }
        interceptor.after(req, resp);
        return resp;
    }
    
}

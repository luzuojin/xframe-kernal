package dev.xframe.http.service.config;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.inject.Synthetic;

@Synthetic
public interface HttpInterceptor {
    
    default Response before(Request req) {
        return null;
    }
    
    default void after(Request req, Response resp) {
        //do nothing
    }

}

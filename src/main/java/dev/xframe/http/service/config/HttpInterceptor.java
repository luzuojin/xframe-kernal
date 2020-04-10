package dev.xframe.http.service.config;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.inject.Synthetic;

@Synthetic
public interface HttpInterceptor {
    
    Response intercept(Request req);
    
}

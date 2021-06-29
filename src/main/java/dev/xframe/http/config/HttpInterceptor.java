package dev.xframe.http.config;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.inject.Composite;

@Composite
public interface HttpInterceptor {
    
    Response intercept(Request req);
    
}

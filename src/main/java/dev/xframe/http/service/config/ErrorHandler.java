package dev.xframe.http.service.config;

import dev.xframe.http.Request;
import dev.xframe.http.Response;

@FunctionalInterface
public interface ErrorHandler {
    
    public Response handle(Request req, Throwable e);
    
}

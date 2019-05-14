package dev.xframe.http.service;

public interface ErrorHandler {
    
    public Response handle(Request req, Throwable e);
    
}

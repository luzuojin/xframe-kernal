package dev.xframe.http.service.config;

public interface ServiceConfig {
    
    ErrorHandler getErrorhandler();

    HttpInterceptor getInterceptor();
    
    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

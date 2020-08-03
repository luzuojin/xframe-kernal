package dev.xframe.http.service.config;

import dev.xframe.inject.Providable;

@Providable
public interface ServiceConfig {
    
    ErrorHandler getErrorhandler();

    HttpInterceptor getInterceptor();
    
    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

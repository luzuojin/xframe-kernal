package dev.xframe.http.config;

import dev.xframe.inject.Providable;

@Providable
public interface HttpConfig {
    
    ErrorHandler getErrorhandler();

    HttpInterceptor getInterceptor();
    
    HttpListener getListener();
    
    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

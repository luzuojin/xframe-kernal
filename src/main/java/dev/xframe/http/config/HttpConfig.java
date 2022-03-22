package dev.xframe.http.config;

import dev.xframe.inject.Providable;

import java.util.concurrent.Executor;

@Providable
public interface HttpConfig {

    Executor getServiceExecutor();

    ErrorHandler getErrorhandler();

    HttpInterceptor getInterceptor();
    
    HttpListener getListener();
    
    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

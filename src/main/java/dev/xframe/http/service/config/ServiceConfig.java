package dev.xframe.http.service.config;

public interface ServiceConfig {
    
    ErrorHandler getErrorhandler();

    HttpInterceptor getInterceptor();
    
    FileHandler getFileHandler();
    
    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

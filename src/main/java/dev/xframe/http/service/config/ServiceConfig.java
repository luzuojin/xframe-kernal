package dev.xframe.http.service.config;

public interface ServiceConfig {
    
    ErrorHandler getErrorhandler();

    RequestInteceptor getInteceptor();
    
    FileHandler getFileHandler();
    
    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

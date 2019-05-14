package dev.xframe.http.service;

public interface ServiceConfig {
    
    ErrorHandler getErrorhandler();

    RequestInteceptor getInteceptor();
    
    FileHandler getFileHandler();

}

package dev.xframe.http.service;

public interface ServiceBuilder {

    public Service build(Class<?> clazz) throws Exception;
    
}

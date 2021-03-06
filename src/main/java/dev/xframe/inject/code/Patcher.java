package dev.xframe.inject.code;

import javassist.CtClass;

public interface Patcher {
    
    public boolean required(CtClass clazz) throws Exception;
    
    public void patch(CtClass clazz) throws Exception;

}

package dev.xframe.injection.code;

import javassist.CtClass;

public interface Patcher {
    
    public boolean required(CtClass clazz) throws Throwable;
    
    public void patch(CtClass clazz) throws Throwable;

}

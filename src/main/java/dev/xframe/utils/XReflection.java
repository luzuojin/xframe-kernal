package dev.xframe.utils;

public class XReflection extends SecurityManager {
	
	static final XReflection relection = new XReflection();
    @Override
    protected Class<?>[] getClassContext() {
        return super.getClassContext();
    }
    
    public static Class<?> getCallerClass() {
        return getCallerClass(2);
    }
    
    public static Class<?> getCallerClass(int depth) {
        Class<?>[] classes = relection.getClassContext();
        int index = depth + 2;
        int len = classes.length;
        return len > index ? classes[index] : classes[len - 1];
    }
    
}
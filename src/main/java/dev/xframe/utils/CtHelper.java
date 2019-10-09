package dev.xframe.utils;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class CtHelper {
    
    public static CtMethod copy(CtMethod src, String body, CtClass declaring) throws CannotCompileException, NotFoundException {
        return CtNewMethod.make(src.getModifiers(), src.getReturnType(), src.getName(), src.getParameterTypes(), src.getExceptionTypes(), body, declaring);
    }
    
    public static CtConstructor copy(CtConstructor src, String body, CtClass declaring) throws CannotCompileException, NotFoundException {
    	return CtNewConstructor.make(src.getParameterTypes(), src.getExceptionTypes(), body, declaring);
    }

    public static boolean isObjectType(CtClass clazz) {
        return Object.class.getName().equals(clazz.getName());
    }
    
    public static boolean isObjectType(Class<?> clazz) {
        return Object.class.equals(clazz);
    }
    
    public static boolean isDefault(CtMethod method) {
        return ((method.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC) && method.getDeclaringClass().isInterface();
    }
    
}

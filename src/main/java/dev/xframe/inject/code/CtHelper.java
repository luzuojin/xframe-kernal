package dev.xframe.inject.code;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class CtHelper {

    private static class InternalClassPool extends ClassPool {
        private Method defineCls = XReflection.getMethod(ClassLoader.class, "defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
        public InternalClassPool() {super(true);}
        @Override
        public Class<?> toClass(CtClass ct, Class<?> neighbor, ClassLoader loader, ProtectionDomain domain) throws CannotCompileException {
            try {
                byte[] bytecode = ct.toBytecode();
                return (Class<?>) defineCls.invoke(loader, ct.getName(), bytecode, 0, bytecode.length, domain);
            } catch (Exception e) {
                throw XCaught.throwException(e);
            }
        }
    }
    private static final ClassPool DefaultClassPool = new InternalClassPool();

    public static ClassPool getClassPool() {
        return DefaultClassPool;
    }
    public static ClassPool newClassPool() {
        return new InternalClassPool();
    }

    public static CtMethod copy(CtMethod src, String body, CtClass declaring) throws CannotCompileException, NotFoundException {
        return CtNewMethod.make(src.getModifiers(), src.getReturnType(), src.getName(), src.getParameterTypes(), src.getExceptionTypes(), body, declaring);
    }

    public static CtConstructor copy(CtConstructor src, String body, CtClass declaring) throws CannotCompileException, NotFoundException {
        return CtNewConstructor.make(src.getParameterTypes(), src.getExceptionTypes(), body, declaring);
    }
    
    public static String wrapParams(int len) {
        return wrapParams(len, 0);
    }
    public static String wrapParams(int len, int skip) {
        return String.join(",", IntStream.rangeClosed(skip + 1, len).mapToObj(i->"$"+i).collect(Collectors.toList()));
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
    
    public static boolean isStatic(CtMethod method) {
        return (method.getModifiers() & Modifier.STATIC) > 0;
    }
    
    /**
     * define class before gen class
     */
    public static Class<?> defineClass(String clsName) {
        try {
            return Class.forName(clsName);
        } catch (ClassNotFoundException e) {
            //ignore
            return null;
        }
    }

}

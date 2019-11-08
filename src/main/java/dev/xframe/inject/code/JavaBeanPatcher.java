package dev.xframe.inject.code;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

public class JavaBeanPatcher implements Patcher {

    static boolean isDispensablePatchTransient(CtClass clazz) throws NotFoundException, ClassNotFoundException {
        JavaBean anno = getAnnotation(clazz);
        return (anno == null) ? true : !(((JavaBean) clazz.getAnnotation(JavaBean.class)).value());
    }
    
    static JavaBean getAnnotation(CtClass clazz) throws ClassNotFoundException {
        return (JavaBean) clazz.getAnnotation(JavaBean.class);
    }
    
    @Override
    public boolean required(CtClass clazz) throws NotFoundException, ClassNotFoundException {
        return getAnnotation(clazz) != null;
    }
    
    @Override
    public void patch(CtClass clazz) throws CannotCompileException, NotFoundException, ClassNotFoundException {
        CtField[] fields = clazz.getDeclaredFields();
        for (CtField field : fields) {
            int modifiers = field.getModifiers();
            if(Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || (Modifier.isTransient(modifiers) && isDispensablePatchTransient(clazz))) continue;
            makeSetter(clazz, field);
            makeGetter(clazz, field);
        }
    }

    private void makeGetter(CtClass clazz, CtField field) throws NotFoundException, CannotCompileException {
        if(!hasGetterMethod(clazz, field)) {
            clazz.addMethod(CtNewMethod.getter(specsName("get", field.getName()), field));
        }
    }

    private boolean hasGetterMethod(CtClass clazz, CtField field) throws NotFoundException {
        String name = field.getName();
        if(CtClass.booleanType.equals(field.getType())) {
            if(name.startsWith("is")) {
                if(hasMethod0(clazz, name, field.getType(), null)) return true;
            } else if(hasMethod0(clazz, specsName("is", name), field.getType(), null)) {
                return true;
            }
        }
        return hasMethod0(clazz, specsName("get", name), field.getType(), null);
    }

    private boolean hasMethod0(CtClass clazz,  String name, CtClass returnType, CtClass paramType) {
        try {
            clazz.getMethod(name, Descriptor.ofMethod(returnType, paramType == null ? null : new CtClass[]{paramType}));
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }
    
    private String specsName(String prefix, String name) {
        char first = name.charAt(0);
        if(Character.isUpperCase(first)) {
            return prefix + name;
        }
        if(name.length() > 1) {
            char second = name.charAt(1);
            if(Character.isUpperCase(second)) {
                return prefix + name;
            }
        }
        return prefix + Character.toString(Character.toUpperCase(first)) + name.substring(1);
    }

    private void makeSetter(CtClass clazz, CtField field) throws NotFoundException, CannotCompileException {
        if(!hasSetterMethod(clazz, field)) {
            clazz.addMethod(CtNewMethod.setter(specsName("set", field.getName()), field));
        }
    }

    private boolean hasSetterMethod(CtClass clazz, CtField field) throws NotFoundException {
        String name = field.getName();
        return hasMethod0(clazz, specsName("set", name), CtClass.voidType, field.getType());
    }

}

package dev.xframe.injection.code;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.injection.Prototype;
import dev.xframe.utils.CtHelper;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;

public class PrototypePatcher implements Patcher {
    
    private static final InjectionCode CODE = new InjectionCode(Injection.class);
    
    /**
     * 当前类是Prototype
     *   1: 有需要Inject的字段
     *   2: 父类不是Prototype并且有需要Inject的字段
     */
    public boolean required(CtClass clazz) throws NotFoundException {
        if(isPrototype(clazz)) {
            CtClass t = clazz;
            do {
                CtField[] fields = t.getDeclaredFields();
                for (CtField ctField : fields) {
                    if(ctField.hasAnnotation(Inject.class)) return true;
                }
                t = t.getSuperclass();
            } while(!isPrototype(t) && !CtHelper.isObjectType(t));
        }
        return false;
    }
    
    private boolean isPrototype(CtClass clazz) throws NotFoundException {
        CtClass t = clazz;
        while(!Object.class.getName().equals(t.getName())) {
            if(t.hasAnnotation(Prototype.class)) return true;
            t = t.getSuperclass();
        }
        return false;
    }

    @Override
    public void patch(CtClass clazz) throws CannotCompileException, NotFoundException {
        clazz.addField(CtField.make(CODE.field(clazz.getName()), clazz));
        
        CtConstructor[] constructors = clazz.getDeclaredConstructors();
        for (CtConstructor ctConstructor : constructors) {
            final SuperCall superCall = new SuperCall();
            ctConstructor.instrument(new ExprEditor() {
                @Override
                public void edit(ConstructorCall c) throws CannotCompileException {
                    if (c.isSuper())
                        superCall.is = true;
                }
            });
            if (superCall.is) {
                ctConstructor.insertBeforeBody(CODE.call("this, %s"));
            }
        }
    }
    
    static class SuperCall {
        boolean is = false;
    }
    
}

package dev.xframe.inject.code;

import java.util.Map;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;

public class PrototypePatcher implements Patcher {
    
    static final Map<String, String> cts = CtParser.parse("prototype.ct");
    
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
        clazz.addField(CtField.make(cts.get("injector_field").replace("${prototype.classname}", clazz.getName()), clazz));
        
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
                ctConstructor.insertBeforeBody(cts.get("injector_invoke"));
            }
        }
    }
    
    static class SuperCall {
        boolean is = false;
    }
    
}

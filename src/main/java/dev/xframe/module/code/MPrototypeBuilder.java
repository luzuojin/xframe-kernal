package dev.xframe.module.code;

import dev.xframe.inject.Prototype;
import dev.xframe.inject.code.InjectionCode;
import dev.xframe.module.ModularInjection;
import dev.xframe.module.ModuleContainer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * 
 * Generate module prototype add inject code
 * 
 * @author luzj
 *
 */
public class MPrototypeBuilder {
    
    public static Class<?> build(Class<?> clazz) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(clazz.getName());

            CtClass ct = pool.makeClass(clazz.getName() + "$Proxy");
            ct.setSuperclass(ctParent);
            
            ClassFile ccfile = ct.getClassFile();
            ConstPool ccpool = ccfile.getConstPool();
            
            AnnotationsAttribute attr = new AnnotationsAttribute(ccpool, AnnotationsAttribute.visibleTag);
            attr.addAnnotation(new Annotation(ccpool, pool.get(Prototype.class.getName())));
            ccfile.addAttribute(attr);
            
            makeInjectCode(clazz, pool, ct);
            
            return ct.toClass();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    static final InjectionCode CODE = new InjectionCode(ModularInjection.class);
    
    static void makeInjectCode(Class<?> clazz, ClassPool pool, CtClass ct) throws Exception {
        ct.addField(CtField.make(CODE.field(clazz.getName()), ct));
        
        CtConstructor co = CtNewConstructor.make(new CtClass[]{pool.get(ModuleContainer.class.getName())}, new CtClass[0], ct);
        co.setBody(CODE.call("this, %s, $1"));
        ct.addConstructor(co);
    }

}

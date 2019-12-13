package dev.xframe.module.code;

import java.util.Map;

import dev.xframe.inject.Prototype;
import dev.xframe.utils.CtParser;
import dev.xframe.utils.XCaught;
import javassist.ClassPool;
import javassist.CtClass;
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
	
    static final Map<String, String> cts = CtParser.parse("module.ct");
	
    public static Class<?> build(Class<?> clazz) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(clazz.getName());

            String className = cts.get("module_name").replace("${module_basic}", clazz.getName());
            CtClass ct = pool.makeClass(className);
            ct.setSuperclass(ctParent);
            
            ClassFile ccfile = ct.getClassFile();
            ConstPool ccpool = ccfile.getConstPool();
            
            AnnotationsAttribute attr = new AnnotationsAttribute(ccpool, AnnotationsAttribute.visibleTag);
            attr.addAnnotation(new Annotation(ccpool, pool.get(Prototype.class.getName())));
            ccfile.addAttribute(attr);
            
            ct.addField(CtField.make(cts.get("modular_injector_field").replace("${module_basic}", clazz.getName()), ct));
            
			ct.addConstructor(CtNewConstructor.make(cts.get("module_constructor").replace("${module_simple_name}", ct.getSimpleName()), ct));
            
            return ct.toClass();
        } catch (Exception e) {
            return XCaught.throwException(e);
        }
    }

}

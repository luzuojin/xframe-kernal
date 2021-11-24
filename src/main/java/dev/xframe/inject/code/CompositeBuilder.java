package dev.xframe.inject.code;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Consumer;

import dev.xframe.inject.Composite;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class CompositeBuilder {
    
    static final Map<String, String> cts = CtParser.parse("composite.ct");
    
    public static void remove(Object composite, Object delegate) {
        ((IComposite) composite)._removeDelegate(delegate);
    }
    public static void append(Object composite, Object delegate) {
        ((IComposite) composite)._appendDelegate(delegate);
    }
    public static void forEach(Object composite, Consumer<?> consumer) {
        ((IComposite) composite)._forEachDeletage(consumer);
    }

    public static Object buildBean(Class<?> compositeCls) {
        try {
            return XReflection.newInstance(buildClass(compositeCls));
        } catch (Exception e) {
            return XCaught.throwException(e);
        }
    }
    
    public synchronized static Class<?> buildClass(Class<?> clazz) {
        return buildClass(clazz, true, clazz.getAnnotation(Composite.class).ignoreError(), clazz.getAnnotation(Composite.class).boolByTrue());
    }
    public synchronized static Class<?> buildClass(Class<?> cclazz, boolean invokable, boolean ignoreError, boolean boolByTrue) {
        try {
            String basicName = cclazz.getName();
			String proxyName = cts.get("composite_name").replace("${composite_basic}", basicName);

            Class<?> proxyClass = null;
            try {
                proxyClass = Class.forName(proxyName);
            } catch(ClassNotFoundException e) {}

            if(proxyClass == null) {
            	ClassPool pool = CtHelper.getClassPool();
                CtClass cc = pool.makeClass(proxyName);
                
                CtClass cp = pool.get(basicName);
                if(Modifier.isInterface(cp.getModifiers())) {
                    cc.addInterface(cp);
                } else {
                    cc.setSuperclass(cp);
                }
                
                cc.addInterface(pool.get(IComposite.class.getName()));

                cc.addField(CtField.make(cts.get("logger_field").replace("${composite_basic}", basicName), cc));
                cc.addField(CtField.make(cts.get("delegates_field"), cc));

                cc.addMethod(CtNewMethod.make(cts.get("append_delegate_method"), cc));
                cc.addMethod(CtNewMethod.make(cts.get("remove_delegate_method"), cc));
                
                String feIvk = ignoreError ? cts.get("fe_invoke_ex_part").replace("${fe_invoke_ex_part}", cts.get("fe_invoke_part")) : cts.get("fe_invoke_part");
                cc.addMethod(CtNewMethod.make(cts.get("foreach_delegate_method").replace("${composite_basic}", basicName).replace("${fe_invoke_part}", feIvk), cc));
                
                for (CtMethod ctMethod : cp.getMethods()) {
                    if(CtHelper.isObjectType(ctMethod.getDeclaringClass())) continue;
                    if(Modifier.isStatic(ctMethod.getModifiers())) continue;
                    if(invokable) {
                        CtClass rtype = ctMethod.getReturnType();
                        
                        String invk = cts.get(rtype==CtClass.voidType ? "void_invoke_part" : "obj_invoke_part");
                        
                        String rdef = rtype==CtClass.booleanType ? String.valueOf(!boolByTrue) : cts.get(rtype==CtClass.voidType ? "void_rdef" : rtype.isPrimitive() ? "pri_rdef" : "obj_rdef");
                        
                        String body = cts.get("simple_method_body")
                                .replace("${obj_invoke_part}", ignoreError ? cts.get("obj_invoke_ex_part").replace("${obj_invoke_ex_part}", invk) : invk)
                                .replace("${composite_basic}", basicName)
                                .replace("${method_name}", ctMethod.getName())
                                .replace("${method_params}", CtHelper.wrapParams(ctMethod.getParameterTypes().length))
                                .replace("${return_class}", rtype.getName())
                                .replace("${return_default}", rdef);
                        
                        cc.addMethod(CtHelper.copy(ctMethod, body, cc));
                    } else if(!CtHelper.isDefault(ctMethod)) {
                        cc.addMethod(CtHelper.copy(ctMethod, cts.get("simple_none_invoke_method_body"), cc));
                    }
                }
                proxyClass = cc.toClass();
            }
            return proxyClass;
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    
    public static interface IComposite {
        public void _appendDelegate(Object delegate);
        public void _removeDelegate(Object delegate);
        public void _forEachDeletage(Consumer<?> consumer);
    }

}

package dev.xframe.injection.code;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import dev.xframe.injection.Synthetic;
import dev.xframe.utils.CtHelper;
import dev.xframe.utils.CtParser;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class SyntheticBuilder {
    
    static final Map<String, String> cts = CtParser.parse("synthetic.ct");
    
    public static void remove(Object synthetic, Object delegate) {
        ((ISynthetic) synthetic)._removeDelegate(delegate);
    }
    public static void append(Object synthetic, Object delegate) {
        ((ISynthetic) synthetic)._appendDelegate(delegate);
    }
    public static void forEach(Object synthetic, Consumer<?> consumer) {
        ((ISynthetic) synthetic)._forEachDeletage(consumer);
    }

    public static Object buildBean(Class<?> syntheticClazz) {
        try {
            return buildClass(syntheticClazz).newInstance();
        } catch (Exception e) {throw new IllegalArgumentException(e);}
    }
    
    public synchronized static Class<?> buildClass(Class<?> clazz) {
        return buildClass(clazz, true, clazz.getAnnotation(Synthetic.class).ignoreError(), clazz.getAnnotation(Synthetic.class).boolByTrue());
    }
    public synchronized static Class<?> buildClass(Class<?> cclazz, boolean invokable, boolean ignoreError, boolean boolByTrue) {
        try {
            String basicName = cclazz.getName();
			String proxyName = cts.get("synthetic_name").replace("${synthetic_basic}", basicName);

            Class<?> proxyClass = null;
            try {
                proxyClass = Class.forName(proxyName);
            } catch(ClassNotFoundException e) {}

            if(proxyClass == null) {
            	ClassPool pool = ClassPool.getDefault();
                CtClass cc = pool.makeClass(proxyName);
                
                CtClass cp = pool.get(basicName);
                if(Modifier.isInterface(cp.getModifiers())) {
                    cc.addInterface(cp);
                } else {
                    cc.setSuperclass(cp);
                }
                
                cc.addInterface(pool.get(ISynthetic.class.getName()));

                cc.addField(CtField.make(cts.get("logger_field").replace("${synthetic_basic}", basicName), cc));
                cc.addField(CtField.make(cts.get("delegates_field"), cc));

                cc.addMethod(CtNewMethod.make(cts.get("append_delegate_method"), cc));
                cc.addMethod(CtNewMethod.make(cts.get("remove_delegate_method"), cc));
                
                String feIvk = ignoreError ? cts.get("fe_invoke_ex_part").replace("${fe_invoke_ex_part}", cts.get("fe_invoke_part")) : cts.get("fe_invoke_part");
                cc.addMethod(CtNewMethod.make(cts.get("foreach_delegate_method").replace("${synthetic_basic}", basicName).replace("${fe_invoke_part}", feIvk), cc));
                
                for (CtMethod ctMethod : cp.getMethods()) {
                    if(CtHelper.isObjectType(ctMethod.getDeclaringClass())) continue;
                    if(Modifier.isStatic(ctMethod.getModifiers())) continue;
                    if(invokable) {
                        CtClass rtype = ctMethod.getReturnType();
                        
                        String args = String.join(",", IntStream.rangeClosed(1, ctMethod.getParameterTypes().length).mapToObj(idx->"$"+idx).toArray(String[]::new));
                        
                        String invk = cts.get(rtype==CtClass.voidType ? "void_invoke_part" : "obj_invoke_part");
                        
                        String rdef = rtype==CtClass.booleanType ? String.valueOf(!boolByTrue) : cts.get(rtype==CtClass.voidType ? "void_rdef" : rtype.isPrimitive() ? "pri_rdef" : "obj_rdef");
                        
                        String body = cts.get("simple_method_body")
                                .replace("${obj_invoke_part}", ignoreError ? cts.get("obj_invoke_ex_part").replace("${obj_invoke_ex_part}", invk) : invk)
                                .replace("${synthetic_basic}", basicName)
                                .replace("${method_name}", ctMethod.getName())
                                .replace("${method_params}", args)
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
            throw new IllegalArgumentException(e);
        }
    }
    
    public static interface ISynthetic {
        public void _appendDelegate(Object delegate);
        public void _removeDelegate(Object delegate);
        public void _forEachDeletage(Consumer<?> consumer);
    }

}

package dev.xframe.module.code;

import dev.xframe.module.ModularConext;
import dev.xframe.utils.CtHelper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * 
 * Generate ModuleContainerFactory by given interface
 * 
 * @author luzj
 *
 */
public class MFactoryBuilder {

    @SuppressWarnings("unchecked")
    public static <T> T build(Class<T> mcFactoryInterface) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(mcFactoryInterface.getName());

            CtClass ct = pool.makeClass(mcFactoryInterface.getName() + "$Proxy");
            ct.addInterface(ctParent);

            CtMethod[] methods = ctParent.getDeclaredMethods();
            assert methods.length == 1;
            CtMethod pmethod = methods[0];
            
            CtClass[] params = pmethod.getParameterTypes();
            StringBuilder body = new StringBuilder("{return new ").append(ModularConext.getMCClassName()).append("(");
            for (int i = 0; i < params.length; i++) {
                if(i > 0) body.append(",");
                body.append("$").append(i+1);
            }
            body.append(");}");
            
            ct.addMethod(CtHelper.copy(pmethod, body.toString(), ct));

            return (T) (ct.toClass().newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}

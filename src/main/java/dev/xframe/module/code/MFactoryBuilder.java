package dev.xframe.module.code;

import java.util.Map;

import dev.xframe.module.ModularConext;
import dev.xframe.utils.CtParser;
import dev.xframe.utils.XCaught;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;

/**
 * 
 * Generate ModuleContainerFactory by given interface
 * 
 * @author luzj
 *
 */
public class MFactoryBuilder {

    static final Map<String, String> cts = CtParser.parse("mcfactory.ct");
    
    @SuppressWarnings("unchecked")
    public static <T> T build(Class<T> mcFactoryInterface) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(mcFactoryInterface.getName());

            CtClass ct = pool.makeClass(cts.get("impl_name"));
            ct.addInterface(ctParent);

            ct.addField(CtField.make(cts.get("injector_field").replace("${mcname}", ModularConext.getMCClassName()), ct));
            
            ct.addMethod(CtNewMethod.make(cts.get("impl_method").replace("${mcname}", ModularConext.getMCClassName()), ct));

            return (T) (ct.toClass().newInstance());
        } catch (Exception e) {
            return XCaught.throwException(e);
        }
    }
    
}

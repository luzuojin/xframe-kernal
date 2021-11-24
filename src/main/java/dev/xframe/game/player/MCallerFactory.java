package dev.xframe.game.player;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import dev.xframe.inject.code.CtHelper;
import dev.xframe.inject.code.CtParser;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * 
 * call module method proxy (as Global bean)
 * 
 * CallerFunc:  dosomething(Player, param1, param2...)
 * ImplMethod:  dosomething(param1, param2...)
 * 
 * bride for CallerFunc to invoke ImplMethod
 * 
 * @author luzj
 *
 */
public class MCallerFactory {
    
    private static final Map<String, String> cts = CtParser.parse("mcaller.ct");
    
    public static <T> T make(Class<T> funcCls, Class<?> moduleCls, int moduleIndex, Method method) {
        try {
            String callerName = cts.get("caller_name").replace("${caller_interfaze}", funcCls.getName());
            Class<?> callerCls = defineClass(callerName);
            if(callerCls == null) {
                ClassPool pool = CtHelper.getClassPool();
                CtClass cParent = pool.get(funcCls.getName());
                CtClass ctClass = pool.makeClass(callerName);
                if(funcCls.isInterface()) {
                    ctClass.addInterface(cParent);
                } else {
                    ctClass.setSuperclass(cParent);
                }
                CtMethod ctMethod = Arrays.stream(cParent.getDeclaredMethods()).filter(cm->isMatched(cm, method)).findAny().get();//may null?
                String ctBody = cts.get("call_method_body")
                        .replace("${module_name}", moduleCls.getName())
                        .replace("${module_index}", String.valueOf(moduleIndex))
                        .replace("${call_part}", cts.get(method.getReturnType()==void.class?"void_call_part":"obj_call_part"))
                        .replace("${method_name}", method.getName())
                        .replace("${method_params}", CtHelper.wrapParams(ctMethod.getParameterTypes().length, 1));
                ctClass.addMethod(CtHelper.copy(ctMethod, ctBody, ctClass));
                
                callerCls = ctClass.toClass();
            }
            return XReflection.newInstance(callerCls);
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    
    /**
     * @param cm      (player, a, b) 
     * @param method          (a, b)
     * @return
     */
    private static boolean isMatched(CtMethod cm, Method method) {
        try {
            return cm.getReturnType().getName().equals(method.getReturnType().getName()) && 
                    Arrays.deepEquals(
                        Arrays.stream(cm.getParameterTypes()).skip(1).map(CtClass::getName).toArray(),
                        Arrays.stream(method.getParameterTypes()).map(Class::getName).toArray());
        } catch (NotFoundException e) {
            throw XCaught.throwException(e);
        }
    }
    
    private static Class<?> defineClass(String callerName) {
        try {
            return Class.forName(callerName);
        } catch (ClassNotFoundException e) {}//ignore
        return null;
    }

}

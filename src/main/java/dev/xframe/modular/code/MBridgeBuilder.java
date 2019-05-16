package dev.xframe.modular.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dev.xframe.modular.ModularBridge;
import dev.xframe.modular.ModularEnigne;
import dev.xframe.modular.ModuleContainer;
import dev.xframe.modular.ModuleTypeLoader;
import dev.xframe.tools.CtHelper;
import dev.xframe.tools.Generic;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;

/**
 * build modular bridge executor clazz
 * @author luzj
 */
public class MBridgeBuilder {
    
    private static String LOADER_NAME = "_moduleLoader";

    public static Class<?> build(Class<?> clazz) throws Exception {
    	return setupLoader(build0(clazz).toClass());
    }

    public static CtClass build0(Class<?> clazz) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClazz = pool.get(clazz.getName());

        //Override method
        //bridge method must override inject method or is the same method
        CtMethod ovvrideMethod = findModularBridgeMethod(ctClazz, ModularBridge.Source.class);
        CtMethod  bridgeMethod = findModularBridgeMethod(ctClazz, ModularBridge.Dest.class);
        
        CtClass proxyClazz = pool.makeClass(clazz.getName() + "$Bridge");
        proxyClazz.setSuperclass(ctClazz);
        
        CtField ctField = new CtField(pool.get(ModuleTypeLoader.class.getName()), LOADER_NAME, proxyClazz);
        ctField.setModifiers(Modifier.STATIC);
        proxyClazz.addField(ctField);
        
        proxyClazz.addMethod(CtHelper.copy(ovvrideMethod, makeBridgeCode(ovvrideMethod, bridgeMethod), proxyClazz));
        return proxyClazz;
    }

    public static Class<?> setupLoader(Class<?> clazz) throws NoSuchFieldException, IllegalAccessException, Exception {
        Field field = clazz.getDeclaredField(LOADER_NAME);
        field.setAccessible(true);
        field.set(null, ModularEnigne.getLoader(findModuleType(clazz)));
        return clazz;
    }

    public static Class<?> findModuleType(Class<?> clazz) throws Exception {
        Method method = findModularBridgeMethod(clazz, ModularBridge.Dest.class);
        Class<?>[] types = Generic.parse(clazz, method.getDeclaringClass()).getParameterTypes(method);
        Annotation[][] annss = method.getParameterAnnotations();
        for (int i = 0; i < annss.length; i++) {
            Annotation[] anns = annss[i];
            for (Annotation ann : anns) {
                if(ann.annotationType().equals(ModularBridge.Bridging.class)) {
                    return types[i];
                }
            }
        }
        return null;
    }
    
    private static Method findModularBridgeMethod(Class<?> clazz, Class<? extends Annotation> type) throws Exception {
        while(!Object.class.equals(clazz)) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if(method.isAnnotationPresent(type)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    
    private static CtMethod findModularBridgeMethod(CtClass clazz, Class<? extends Annotation> type) throws Exception {
        while(!CtHelper.isObjectType(clazz)) {
            CtMethod[] methods = clazz.getDeclaredMethods();
            for (CtMethod method : methods) {
                if(method.getAnnotation(type) != null) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
    
    private static String makeBridgeCode(CtMethod overrideMethod, CtMethod bridgeMethod) throws Exception {
        CtClass[] overrideTypes = overrideMethod.getParameterTypes();
        CtClass[] bridgeTypes = bridgeMethod.getParameterTypes();
        
        StringBuilder sb = new StringBuilder("{").append(bridgeMethod.getName()).append("(");//call method
        int paramIndex = 0;
        for (int i = 0; i < bridgeTypes.length; i++) {
            if(i > 0) sb.append(",");
            if(isBridgeTransfer(bridgeMethod.getParameterAnnotations()[i])) {
                makeLoadModuleCode(sb, findMCIndex(overrideMethod));
                continue;
            }
            if(!overrideTypes[paramIndex].equals(bridgeTypes[i])) {//如果类型不一样 需要转换
                sb.append("(").append(bridgeTypes[i].getName()).append(")");
            }
            sb.append("$").append(paramIndex+1);
            ++ paramIndex;
        }
        return sb.append(");}").toString();
    }

    private static int findMCIndex(CtMethod method) throws Exception {
        int index = 0;
        for (Object[] objects : method.getParameterAnnotations()) {
            ++index;
            if(isBridgeTransfer(objects)) return index;
        }
        throw new IllegalArgumentException("Bridge.Source method require contains one parameter annotation persient by Bridge.Bridging");
    }

    private static boolean isBridgeTransfer(Object[] objects) {
        for (Object object : objects) {
            if(object instanceof ModularBridge.Bridging) return true;
        }
        return false;
    }

    private static void makeLoadModuleCode(StringBuilder sb, int mcIndex) {//_moduleLoader.load((dev.xframe.modular.ModuleContainer)$1)
        sb.append(LOADER_NAME).append(".").append(ModuleTypeLoader.class.getDeclaredMethods()[0].getName()).append("((").append(ModuleContainer.class.getName()).append(")$").append(mcIndex).append(")");
    }
    
}

package dev.xframe.inject.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.utils.CtHelper;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * 
 * 生成@Factory对应的实例
 * @author luzj
 *
 */
public class FactoryBuilder {
    
    public synchronized static Object build(Class<?> factoryInteface, List<Class<?>> classes) {
        Factory anno = factoryInteface.getAnnotation(Factory.class);
        Class<? extends Annotation> type = anno.value();
        List<Class<?>> elements = classes.stream().filter(c->c.isAnnotationPresent(type)).collect(Collectors.toList());
        return buildBySwitchCase(factoryInteface, type, elements, anno.keyInConstructor());
    }

    private static Object buildBySwitchCase(Class<?> factoryInteface, Class<? extends Annotation> annoType, List<Class<?>> elements, boolean keyInConstructor) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.getCtClass(factoryInteface.getName());
            String factoryName = factoryInteface.getName() + "$Factory";
            
            Class<?> dc = defineClass(factoryName);
            if(dc != null) {
                return XReflection.newInstance(dc);
            }
            
            CtClass ct = pool.makeClass(factoryName);
            ct.addInterface(ctParent);
            
            Method method = annoType.getDeclaredMethods()[0];//annoation has one method
            Class<?> keyType = method.getReturnType();
            
            CtMethod[] cms = ctParent.getDeclaredMethods();
            for (CtMethod cm : cms) {
                if(cm.getParameterTypes().length == 0) continue;
                
                String params = buildParams(cm, keyInConstructor);
                String key = isCaseTypeEnum(keyType) ? ("((Enum)$1).ordinal()") : "$1";
                
                StringBuilder body = new StringBuilder();
                body.append("switch(").append(key).append(") {");
                
                for (Class<?> clazz : elements) {
                    Object val = method.invoke(clazz.getAnnotation(annoType));
                    if(keyType.isArray()) {
                        for (int i = 0; i < Array.getLength(val); i++) {
                            appendCaseReturn(body, Array.get(val, i), clazz, params);
                        }
                    } else {
                        appendCaseReturn(body, val, clazz, params);
                    }
                }
                
                appendDefaultReturn(body, factoryInteface.getAnnotation(Factory.class).defaultType(), params);
                
                body.append("}");
                
                ct.addMethod(CtHelper.copy(cm, body.toString(), ct));
            }
            
            return XReflection.newInstance(ct.toClass());
        } catch (Throwable e) {
            return XCaught.throwException(e);
        }
    }
    
    private static Class<?> defineClass(String name) {
        Class<?> proxyClass = null;
        try {
            proxyClass = Class.forName(name);
        } catch(ClassNotFoundException e) {}
        return proxyClass;
    }
    
    private static boolean isCaseTypeEnum(Class<?> keyType) {
        return keyType.isArray() ? keyType.getComponentType().isEnum() : keyType.isEnum();
    }

    private static void appendDefaultReturn(StringBuilder body, Class<?> defClazz, String params) {
        if(Class.class.equals(defClazz)) {
            body.append("default: return null;");
        } else {
            body.append("default: return new ").append(defClazz.getName()).append("(").append(params).append(");");
        }
    }
    
    private static void appendCaseReturn(StringBuilder body, Object caseVal, Class<?> element, String params) {
        Class<?> caseType = caseVal.getClass();
        if(caseType.isEnum()) {
            body.append("case ").append(((Enum<?>)caseVal).ordinal()).append(":");
            body.append("return new ").append(element.getName()).append("(").append(params).append(");");
        } else {
            body.append("case ").append(caseVal).append(":");
            body.append("return new ").append(element.getName()).append("(").append(params).append(");");
        }
    }

    private static String buildParams(CtMethod cm, boolean keyInConstructor) throws NotFoundException {
        StringBuilder params = new StringBuilder();
        int st = keyInConstructor ? 0 : 1;
        for (int i = st; i < cm.getParameterTypes().length; i++) {
            params.append((i == st ? "" : ",")).append("$").append(i + 1);
        }
        return params.toString();
    }

}

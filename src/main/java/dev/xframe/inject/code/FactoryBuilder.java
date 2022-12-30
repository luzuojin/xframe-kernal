package dev.xframe.inject.code;

import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * 生成@Factory对应的实例
 * @author luzj
 *
 */
public class FactoryBuilder {

    public synchronized static Object build(Class<?> factoryInteface, List<Clazz> clzList) {
        Factory anno = factoryInteface.getAnnotation(Factory.class);
        List<Class<?>> impls = Clazz.filter(clzList, c->c.isAnnotationPresent(anno.value()));
        return buildFactoryImplements(factoryInteface, anno, impls);
    }

    private static Object buildFactoryImplements(Class<?> factoryInteface, Factory factory, List<Class<?>> impls) {
        try {
            ClassPool pool = CtHelper.getClassPool();
            CtClass ctParent = pool.getCtClass(factoryInteface.getName());
            String factoryName = factoryInteface.getName() + "$Factory";
            
            Class<?> dc = CtHelper.defineClass(factoryName);
            if(dc != null) {
                return XReflection.newInstance(dc);
            }
            
            CtClass ct = pool.makeClass(factoryName);
            ct.addInterface(ctParent);
            //只能有一个非default/static方法, 需要多个方法的场景???
            CtMethod cm = Arrays.stream(ctParent.getDeclaredMethods())
                    .filter(c -> !CtHelper.isDefault(c))
                    .filter(c -> !Modifier.isStatic(c.getModifiers()))
                    .findAny().get();
            makeFactoryImplements(ct, cm, factory, impls);
            
            return XReflection.newInstance(ct.toClass());
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }
    
    private static boolean hasDefault(Class<?> defCls) {
        return !Class.class.equals(defCls);
    }
    
    private static void makeFactoryImplements(CtClass ct, CtMethod cm, Factory factory, List<Class<?>> impls) throws Exception {
        int singletonIndexOffset = Integer.MIN_VALUE;
        Class<?> defCls = factory.defaultType();
        //singleton需子类有默认构造函数
        if(factory.singleton() && cm.getParameterTypes().length == 1 && !factory.keyInConstructor()) {//make singleon instances field
            singletonIndexOffset = hasDefault(defCls) ? 1 : 0; 
            //new Object[]{new A()...};
            List<Class<?>> def = hasDefault(defCls) ? Collections.singletonList(defCls) : Collections.emptyList();
            String cf = String.format("Object[] _impls = new Object[]{%s};",
                    Stream.concat(def.stream(), impls.stream()).map(cls->String.format("new %s()", cls.getName())).collect(Collectors.joining(","))
                    );
            ct.addField(CtField.make(cf, ct));
        }
        
        Method caseTypeMethod = factory.value().getDeclaredMethods()[0];//annoation has only one method
        Class<?> keyType = caseTypeMethod.getReturnType();
        String params = makeFactoryParams(cm, factory.keyInConstructor());
        
        String key = isCaseTypeEnum(keyType) ? ("((Enum)$1).ordinal()") : "$1";
        
        StringBuilder body = new StringBuilder();
        body.append("switch(").append(key).append(") {");
        
        for (Class<?> impl : impls) {
            Object val = caseTypeMethod.invoke(impl.getAnnotation(factory.value()));
            if(keyType.isArray()) {
                for (int i = 0; i < Array.getLength(val); i++) {
                    appendCaseReturn(body, Array.get(val, i), impl, params, singletonIndexOffset);
                }
            } else {
                appendCaseReturn(body, val, impl, params, singletonIndexOffset);
            }
            ++ singletonIndexOffset;
        }
        
        appendDefaultReturn(body, defCls, params, singletonIndexOffset);
        
        body.append("}");
        
        ct.addMethod(CtHelper.copy(cm, body.toString(), ct));
    }
    
    private static boolean isCaseTypeEnum(Class<?> keyType) {
        return keyType.isArray() ? keyType.getComponentType().isEnum() : keyType.isEnum();
    }

    private static void appendDefaultReturn(StringBuilder body, Class<?> defCls, String params, int singletonIndexOffset) {
        body.append("default:");
        if(hasDefault(defCls)) {
            if(singletonIndexOffset < 0) {
                body.append(String.format("return new %s(%s);", defCls.getName(), params));
            } else {
                body.append(String.format("return (%s) _impls[%s];", defCls.getName(), 0));
            }
        } else {
            body.append("return null;");
        }
    }
    
    private static void appendCaseReturn(StringBuilder body, Object caseVal, Class<?> impl, String params, int singletonIndexOffset) {
        body.append(String.format("case %s:", caseVal.getClass().isEnum()?enumCaseVal(caseVal):caseVal));
        if(singletonIndexOffset < 0) {
            body.append(String.format("return new %s(%s);", impl.getName(), params));
        } else {
            body.append(String.format("return (%s) _impls[%s];", impl.getName(), singletonIndexOffset));
        }
    }

    private static int enumCaseVal(Object caseVal) {
        return ((Enum<?>)caseVal).ordinal();
    }

    private static String makeFactoryParams(CtMethod cm, boolean keyInConstructor) throws NotFoundException {
        StringJoiner params = new StringJoiner(",");
        int st = keyInConstructor ? 0 : 1;
        for (int i = st; i < cm.getParameterTypes().length; i++) {
            params.add("$" + (i+1));
        }
        return params.toString();
    }

}

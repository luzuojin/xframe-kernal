package dev.xframe.inject.code;

import dev.xframe.utils.XCaught;
import dev.xframe.utils.XReflection;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ProxyBuilder {
    
    static final Map<String, String> cts = CtParser.parse("proxy.ct");
    
    public static <T> T setDelegate(T bean, Object delegate) {
        ((IProxy) bean)._setDelegate(delegate);
        return bean;
    }
    public static Object getDelegate(Object bean) {
        return ((IProxy) bean)._getDelegate();
    }

    public static <T> T build(T bean) {
        return setDelegate(build(bean.getClass()), bean);
    }
    public static <T> T build(Class<?> type) {
        return build0(type, type);
    }
    public static <T> T build(Class<?> type, Object delegate) {
        return setDelegate(build0(type, delegate.getClass()), delegate);
    }
    
    private synchronized static <T> T build0(Class<?> basic, Class<?> delegate) {
        try {
            ClassPool pool = CtHelper.getClassPool();
            CtClass ctParent = pool.get(basic.getName());
            String proxyName = cts.get("proxy_name").replace("${proxy_basic}", delegate.getName());
            
            Class<?> proxyClass = CtHelper.defineClass(proxyName);
            
            if(proxyClass == null) {
                CtClass ctClass = makeCtClassWithParent(pool, ctParent, proxyName);
                
                makeDelegateField(ctClass, delegate);
                
                makeDefaultMethods(ctClass, delegate);
                
                makeMethodsInvokeCode(ctClass, ctParent, delegate);

                proxyClass = ctClass.toClass();
            }
            return XReflection.newInstance(proxyClass);
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }
    
    private static void makeMethodsInvokeCode(CtClass ctClass, CtClass ctParent, Class<?> delegate) throws NotFoundException, CannotCompileException {
        for (CtMethod cm : getProxyMethods(ctParent)) {
            String body = cts.get("simple_method_body").replace("${proxy_delegate}", delegate.getName()).replace("${obj_invoke_part}", makeSimpleInvokeCode(cm));
            ctClass.addMethod(CtHelper.copy(cm, body, ctClass));
        }
    }

    private static String makeSimpleInvokeCode(CtMethod cm) throws NotFoundException {
        return cts.get(cm.getReturnType()==CtClass.voidType ? "void_invoke_part" : "obj_invoke_part")
                .replace("${method_name}", cm.getName())
                .replace("${method_params}", CtHelper.wrapParams(cm.getParameterTypes().length));
    }
    
    private static void makeDelegateField(CtClass ctClass, Class<?> delegate) throws CannotCompileException {
        ctClass.addField(CtField.make(cts.get("delegate_field").replace("${proxy_delegate}", delegate.getName()), ctClass));
    }
    
    private static void makeDefaultMethods(CtClass ctClass, Class<?> delegate) throws CannotCompileException {
        ctClass.addConstructor(CtNewConstructor.make(new CtClass[]{}, new CtClass[0], ctClass));
        ctClass.addMethod(CtNewMethod.make(cts.get("get_delegate_method"), ctClass));
        ctClass.addMethod(CtNewMethod.make(cts.get("set_delegate_method").replace("${proxy_delegate}", delegate.getName()), ctClass));
    }

    private static CtClass makeCtClassWithParent(ClassPool pool, CtClass ctParent, String lazyProxyName) throws CannotCompileException, NotFoundException {
        CtClass ctClass = pool.makeClass(lazyProxyName);
        if(Modifier.isInterface(ctParent.getModifiers())) {
            ctClass.addInterface(ctParent);
        } else {
            ctClass.setSuperclass(ctParent);
        }
        ctClass.addInterface(pool.get(IProxy.class.getName()));
        return ctClass;
    }
    
    private static List<CtMethod> getProxyMethods(CtClass parent) throws NotFoundException {
        Map<String, CtMethod> hash = new HashMap<>();
        getProxyMethods(hash, parent.getName(), parent, new HashSet<>());
        return new ArrayList<>(hash.values());
    }
    
    private static void getProxyMethods(Map<String, CtMethod> hash, String from, CtClass clazz, Set<CtClass> visitedClasses) throws NotFoundException {
        if (Object.class.getName().equals(clazz.getName())) return;
        if (!visitedClasses.add(clazz)) return;

        CtClass[] ifs = clazz.getInterfaces();
        for (CtClass anIf : ifs)
            getProxyMethods(hash, from, anIf, visitedClasses);

        CtClass parent = clazz.getSuperclass();
        if (parent != null)
            getProxyMethods(hash, from, parent, visitedClasses);

        clazz.defrost();
        CtMethod[] methods = clazz.getDeclaredMethods();
        for (CtMethod method : methods) {
            if (isVisible(method.getModifiers(), from, method)) {
                String key = method.getName() + ':' + method.getMethodInfo().getDescriptor();
                CtMethod oldMethod = hash.put(key, method);
                if (null != oldMethod && Modifier.isPublic(oldMethod.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {//取public的方法
                    hash.put(key, oldMethod);
                }
            }
        }
    }
    
    private static boolean isVisible(int mod, String from, CtMember meth) {
        if ((mod & (Modifier.PRIVATE | Modifier.STATIC)) != 0) {
            return false;
        } else if ((mod & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
            return true;
        } else {
            String p = getPackageName(from);
            String q = getPackageName(meth.getDeclaringClass().getName());
            return Objects.equals(p, q);
        }
    }

    private static String getPackageName(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? null : name.substring(0, i);
    }
    
    public static interface IProxy {
        public Object _getDelegate();
        public   void _setDelegate(Object delegate);
    }

}

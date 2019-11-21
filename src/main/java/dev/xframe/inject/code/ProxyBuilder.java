package dev.xframe.inject.code;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.xframe.utils.CtHelper;
import dev.xframe.utils.CtParser;
import dev.xframe.utils.XCaught;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class ProxyBuilder {
    
    static final Map<String, String> cts = CtParser.parse("proxy.ct");
    
    public static <T> T setDelegate(T bean, Object delegate) {
        ((IProxy) bean)._setDelegate(delegate);
        return bean;
    }
    public static Object getDelegate(Object bean) {
        return ((IProxy) bean)._getDelegate();
    }
    public static <T> T setSupplier(T bean, Supplier<?> supplier) {
        ((IProxy) bean)._setSupplier(supplier);
        return bean;
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
    public static <T> T buildBySupplier(Class<T> type, Supplier<?> supplier) {
        return setSupplier(build0(type, type), supplier);
    }
    
    @SuppressWarnings("unchecked")
    private synchronized static <T> T build0(Class<?> basic, Class<?> delegate) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctParent = pool.get(basic.getName());
            String proxyName = cts.get("proxy_name").replace("${proxy_basic}", basic.getName());
            
            Class<?> proxyClass = defineClass(proxyName);
            
            if(proxyClass == null) {
                CtClass ctClass = makeCtClassWithParent(pool, ctParent, proxyName);
                
                makeDelegateField(ctClass, delegate);
                
                makeSupplierField(ctClass);
                
                makeDefaultMethods(ctClass, delegate);
                
                makeMethodsInvokeCode(ctClass, ctParent, delegate);

                proxyClass = ctClass.toClass();
            }
            return (T) proxyClass.getConstructor().newInstance();
        } catch (Throwable e) {
            XCaught.throwException(e);
            return null;
        }
    }
    
    private static Class<?> defineClass(String proxyName) {
        Class<?> proxyClass = null;
        try {
            proxyClass = Class.forName(proxyName);
        } catch(ClassNotFoundException e) {}
        return proxyClass;
    }
    
    private static <T> void makeMethodsInvokeCode(CtClass ctClass, CtClass ctParent, Class<?> delegate) throws NotFoundException, CannotCompileException {
        for (CtMethod cm : getProxyMethods(ctParent)) {
            String body = cts.get("simple_method_body").replace("${proxy_delegate}", delegate.getName()).replace("${obj_invoke_part}", makeSimpleInvokeCode(cm));
            ctClass.addMethod(CtHelper.copy(cm, body, ctClass));
        }
    }

    private static String makeSimpleInvokeCode(CtMethod cm) throws NotFoundException {
        return cts.get(cm.getReturnType()==CtClass.voidType ? "void_invoke_part" : "obj_invoke_part")
                .replace("${method_name}", cm.getName())
                .replace("${method_params}", makeMethodInvokeParams(cm));
    }
    
    private static String makeMethodInvokeParams(CtMethod cm) throws NotFoundException {
        return String.join(",", IntStream.rangeClosed(1, cm.getParameterTypes().length).mapToObj(i->"$"+i).collect(Collectors.toList()));
    }

    private static void makeSupplierField(CtClass ctClass) throws CannotCompileException {
        ctClass.addField(CtField.make(cts.get("delegate_supplier_field"), ctClass));
    }

    private static void makeDelegateField(CtClass ctClass, Class<?> delegate) throws CannotCompileException {
        ctClass.addField(CtField.make(cts.get("delegate_field").replace("${proxy_delegate}", delegate.getName()), ctClass));
    }
    
    private static void makeDefaultMethods(CtClass ctClass, Class<?> delegate) throws CannotCompileException, NotFoundException {
        ctClass.addConstructor(CtNewConstructor.make(new CtClass[]{}, new CtClass[0], ctClass));
        ctClass.addMethod(CtNewMethod.make(cts.get("get_delegate_method"), ctClass));
        ctClass.addMethod(CtNewMethod.make(cts.get("set_delegate_method").replace("${proxy_delegate}", delegate.getName()), ctClass));
        ctClass.addMethod(CtNewMethod.make(cts.get("set_supplier_method"), ctClass));
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
        for (int i = 0; i < ifs.length; i++)
            getProxyMethods(hash, from, ifs[i], visitedClasses);

        CtClass parent = clazz.getSuperclass();
        if (parent != null)
            getProxyMethods(hash, from, parent, visitedClasses);

        clazz.defrost();
        CtMethod[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (isVisible(methods[i].getModifiers(), from, methods[i])) {
                CtMethod m = methods[i];
                String key = m.getName() + ':' + m.getMethodInfo().getDescriptor();
                CtMethod oldMethod = (CtMethod)hash.put(key, methods[i]); 
                if (null != oldMethod && Modifier.isPublic(oldMethod.getModifiers()) && !Modifier.isPublic(methods[i].getModifiers()) ) {//取public的方法
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
            return p == null ? q == null : p.equals(q);
        }
    }

    private static String getPackageName(String name) {
        int i = name.lastIndexOf('.');
        return i < 0 ? null : name.substring(0, i);
    }
    
    public static interface IProxy {
        public Object _getDelegate();
        public   void _setDelegate(Object delegate);
        public   void _setSupplier(Supplier<?> supplier);
    }

}

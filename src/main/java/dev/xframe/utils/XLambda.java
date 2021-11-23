package dev.xframe.utils;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Supplier;

import dev.xframe.inject.code.CtHelper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.MethodInfo;

/**
 * 使用动态的Lambda实例代替Reflection调用
 * 性能接近原生方法调用
 * @author luzj
 */
public class XLambda {

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> createByConstructor(Class<?> clazz) {
        return createByConstructor(Supplier.class, clazz);
    }
    public static <T> T createByConstructor(Class<T> lambdaInterface, Class<?> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<?> constructor = XReflection.getConstructor(clazz, parameterTypes);
            MethodHandles.Lookup lookup = createLookup(clazz);
            MethodHandle methodHandle = lookup.unreflectConstructor(constructor);
            return _create(lambdaInterface, lookup, methodHandle);
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }

    public static <T> T create(Class<T> lambdaInterface, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return create(lambdaInterface, XReflection.getMethod(clazz, methodName, parameterTypes));
    }
    public static <T> T createSpecial(Class<T> lambdaInterface, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return createSpecial(lambdaInterface, XReflection.getMethod(clazz, methodName, parameterTypes));
    }

    public static <T> T create(Class<T> lambdaInterface, Method method) {
        return _create(lambdaInterface, method, false);
    }
    public static <T> T createSpecial(Class<T> lambdaInterface, Method method) {
        return _create(lambdaInterface, method, true);
    }

    private static <T> T _create(Class<?> lambdaInterface, Method method, boolean invokeSpecial) {
        try {
            MethodHandles.Lookup lookup = createLookup(method.getDeclaringClass());
            MethodHandle methodHandle = invokeSpecial? lookup.unreflectSpecial(method, method.getDeclaringClass()) : lookup.unreflect(method);
            return _create(lambdaInterface, lookup, methodHandle);
        } catch (Throwable e) {
            throw XCaught.throwException(e);//can`t return
        }
    }

    private static <T> T _create(Class<?> lambdaInterface, MethodHandles.Lookup lookup, MethodHandle methodHandle) throws LambdaConversionException, Throwable {
        MethodType instantiatedMethodType = methodHandle.type();
        Method interfaceMethod = getInterfaceLambdaMethod(lambdaInterface);
        String signatureName = interfaceMethod.getName();
        MethodType samMethodType = makeMethodTypeGeneric(instantiatedMethodType, interfaceMethod);
        CallSite site = LambdaMetafactory.metafactory(
                lookup,
                signatureName,
                MethodType.methodType(lambdaInterface),
                samMethodType,
                methodHandle,
                instantiatedMethodType);
        return (T) site.getTarget().invoke();
    }

    private static Method getInterfaceLambdaMethod(Class<?> lambdaInterface) {
        assert lambdaInterface.isInterface();
        return Arrays.stream(lambdaInterface.getMethods()).filter(m->!m.isDefault()&&(m.getModifiers()&Modifier.STATIC)==0).findAny().get();
    }

    /**
     * change instantiated method type paramters to generic (Object)
     */
    private static MethodType makeMethodTypeGeneric(MethodType methodType, Method pmethod) {
        MethodType sam = methodType;
        Class<?>[] sparams = sam.parameterArray();
        Class<?>[] pparams = pmethod.getParameterTypes();
        for (int i = 0; i < sparams.length; i++) {
            if (!sparams[i].equals(pparams[i])) {
                sam = sam.changeParameterType(i, pparams[i]);
            }
        }
        if (!pmethod.getReturnType().equals(sam.returnType())) {
            sam = sam.changeReturnType(pmethod.getReturnType());
        }
        return sam;
    }

    private static Lookup createLookup(Class<?> clazz) throws Exception {
        return XLookup.in(clazz);
    }
    
    
    /*--Generate Field Setter&Getter Lambda implement via static final MethodHandle (able to inlined by jit)--*/
    
    public static <T> T createByGetter(Class<T> lambdaInterface, Class<?> refc, String name) {
        return createByGetter(lambdaInterface, XReflection.getField(refc, name));
    }
    @SuppressWarnings("unchecked")
    public static <T> T createByGetter(Class<T> lambdaInterface, Field field) {
        try {
            Class<?> refCls = field.getDeclaringClass();
            Class<?> typCls = field.getType();
            
            String cname = String.format("%s_%s_%sGetter", refCls.getName(), field.getName(), lambdaInterface.hashCode());
            
            Class<?> getterCls = CtHelper.defineClass(cname);
            if(getterCls == null) {
                //make class & add lambda interface
                ClassPool pool = CtHelper.getClassPool();
                CtClass p = pool.get(lambdaInterface.getName());
                CtClass c = pool.makeClass(cname);
                c.addInterface(p);
                
                //make MethodHandle Getter field
                String fname = "_getter";
                String ftemp = "static final java.lang.invoke.MethodHandle %s = dev.xframe.utils.XReflection.findGetter(%s.class, \"%s\", %s.class);";
                c.addField(CtField.make(String.format(ftemp, fname, refCls.getCanonicalName(), field.getName(), typCls.getCanonicalName()), c));
                
                //make implement via MethodHandle.invokeExact
                CtMethod m = CtNewMethod.copy(Arrays.stream(p.getDeclaredMethods()).filter(_m->!(CtHelper.isDefault(_m)||CtHelper.isStatic(_m))).findAny().get(), c, null);
                //use bytecode because javassist do not support varargs
                Bytecode b = new Bytecode(c.getClassFile().getConstPool());
                b.addAload(1);
                b.addCheckcast(binaryToInternal(refCls.getName()));//java/lang/Object
                b.addAstore(2);
                b.addGetstatic(binaryToInternal(cname), fname, "Ljava/lang/invoke/MethodHandle;");//call xxx_field_Getter._getter
                b.addAload(2);
                b.addInvokevirtual("java/lang/invoke/MethodHandle", "invokeExact", String.format("(%s)%s", classToDescriptor(refCls), classToDescriptor(typCls)));//MethodHandle.invokeExact
                b.setMaxLocals(3);
                //try make boxed
                if(!m.getReturnType().isPrimitive() && typCls.isPrimitive()) {//Integer.valueOf
                    Class<?> boxedType = primitiveToBoxedType(typCls);
                    b.addInvokestatic(binaryToInternal(boxedType.getName()), "valueOf", String.format("(%s)%s", classToDescriptor(typCls), classToDescriptor(boxedType)));
                }
                b.addReturn(m.getReturnType());
                
                MethodInfo mi = m.getMethodInfo();
                mi.setCodeAttribute(b.toCodeAttribute());
                mi.setAccessFlags(mi.getAccessFlags() & ~AccessFlag.ABSTRACT);//remove abstract access flag
                c.addMethod(m);
                
                getterCls = c.toClass();
            }
            return (T) getterCls.newInstance();
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    
    public static <T> T createBySetter(Class<T> lambdaInterface, Class<?> refc, String name) {
        return createBySetter(lambdaInterface, XReflection.getField(refc, name));
    }
    @SuppressWarnings("unchecked")
    public static <T> T createBySetter(Class<T> lambdaInterface, Field field) {
        try {
            Class<?> refCls = field.getDeclaringClass();
            Class<?> typCls = field.getType();
            
            String cname = String.format("%s_%s_%sSetter", refCls.getName(), field.getName(), lambdaInterface.hashCode());
            
            Class<?> setterCls = CtHelper.defineClass(cname);
            if(setterCls == null) {
                //make class & add lambda interface
                ClassPool pool = CtHelper.getClassPool();
                CtClass p = pool.get(lambdaInterface.getName());
                CtClass c = pool.makeClass(cname);
                c.addInterface(p);
                
                //make MethodHandle Getter field
                String fname = "_setter";
                String ftemp = "static final java.lang.invoke.MethodHandle %s = dev.xframe.utils.XReflection.findSetter(%s.class, \"%s\", %s.class);";
                c.addField(CtField.make(String.format(ftemp, fname, refCls.getCanonicalName(), field.getName(), typCls.getCanonicalName()), c));
                
                //make implement via MethodHandle.invokeExact
                CtMethod m = CtNewMethod.copy(Arrays.stream(p.getDeclaredMethods()).filter(_m->!(CtHelper.isDefault(_m)||CtHelper.isStatic(_m))).findAny().get(), c, null);
                CtClass mfType = m.getParameterTypes()[1];      //ctmethod field param type(CtClass)
                CtClass jfType = pool.get(typCls.getName());    //java field type to CtClass
                //use bytecode because javassist do not support varargs
                Bytecode b = new Bytecode(c.getClassFile().getConstPool());
                b.addAload(1);
                b.addCheckcast(binaryToInternal(refCls.getName()));//java/lang/Object
                b.addAstore(3);
                b.addLoad(2, mfType);
                //unbox primitive type -- ((Integer) v).intValue()
                if(jfType.isPrimitive()) {
                    if(!mfType.isPrimitive()) {
                        String boxed = binaryToInternal(primitiveToBoxedType(typCls).getName());
                        b.addCheckcast(boxed);
                        b.addInvokevirtual(boxed, String.format("%sValue", typCls.getName()), String.format("()%s", classToDescriptor(typCls)));
                    }//else primitive to primitive can`t cast (l2i... not supported now)
                } else {
                    b.addCheckcast(binaryToInternal(typCls.getName()));
                }
                b.addStore(4, jfType);
                b.addGetstatic(binaryToInternal(cname), fname, "Ljava/lang/invoke/MethodHandle;");//call xxx_field_Getter._getter
                b.addAload(3);
                b.addLoad(4, jfType);
                b.addInvokevirtual("java/lang/invoke/MethodHandle", "invokeExact", String.format("(%s%s)V", classToDescriptor(refCls), classToDescriptor(typCls)));//MethodHandle.invokeExact
                b.setMaxLocals(5);
                b.addReturn(m.getReturnType());
                
                MethodInfo mi = m.getMethodInfo();
                mi.setCodeAttribute(b.toCodeAttribute());
                mi.setAccessFlags(mi.getAccessFlags() & ~AccessFlag.ABSTRACT);//remove abstract access flag
                c.addMethod(m);
                
                setterCls = c.toClass();
            }
            return (T) setterCls.newInstance();
        } catch (Exception e) {
            throw XCaught.throwException(e);
        }
    }
    
    private static String binaryToInternal(String name) {
        return name.replace('.', '/');
    }
    private static String classToDescriptor(Class<?> c) {
        if(c.isArray()) {
            return "[" + classToDescriptor(c.getComponentType());
        }
        if(c.isPrimitive()) {
            if(c == void.class) return "V";
            if(c == boolean.class) return "Z";
            if(c == byte.class) return "B";
            if(c == char.class) return "C";
            if(c == short.class) return "S";
            if(c == int.class) return "I";
            if(c == long.class) return "J";
            if(c == float.class) return "F";
            if(c == double.class) return "D";
            throw new IllegalArgumentException("Unsupported Primitive type: " + c);
        }
        return "L" + binaryToInternal(binaryToInternal(c.getName())) + ";";
    }
    private static Class<?> primitiveToBoxedType(Class<?> c) {
        if(c == void.class) return Void.class;
        if(c == boolean.class) return Boolean.class;
        if(c == byte.class) return Byte.class;
        if(c == char.class) return Character.class;
        if(c == short.class) return Short.class;
        if(c == int.class) return Integer.class;
        if(c == long.class) return Long.class;
        if(c == float.class) return Float.class;
        if(c == double.class) return Double.class;
        throw new IllegalArgumentException("Unsupported Primitive type: " + c);
    }
}

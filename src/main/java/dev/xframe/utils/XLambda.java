package dev.xframe.utils;

import static java.lang.invoke.MethodHandles.Lookup.PACKAGE;
import static java.lang.invoke.MethodHandles.Lookup.PRIVATE;
import static java.lang.invoke.MethodHandles.Lookup.PROTECTED;
import static java.lang.invoke.MethodHandles.Lookup.PUBLIC;

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

/**
 * 使用动态的Lambda实例代替Reflection调用
 * 性能接近原生方法调用
 * @author luzj
 */
public class XLambda {
	
	private static Field lookupAllowedModesField;
	private static final int ALL_MODES = (PRIVATE | PROTECTED | PACKAGE | PUBLIC);
	
	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> createByConstructor(Class<?> clazz) throws Throwable {
		return createByConstructor(Supplier.class, clazz);
	}
	public static <T> T createByConstructor(Class<T> lambdaInterface, Class<?> clazz, Class<?>... parameterTypes) {
		try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            MethodHandles.Lookup lookup = createLookup(clazz);
            MethodHandle methodHandle = lookup.unreflectConstructor(constructor);
            return _create(lambdaInterface, lookup, methodHandle);
        } catch (Throwable e) {
            return XCaught.throwException(e);
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
	
	private static <T> T _create(Class<T> lambdaInterface, Method method, boolean invokeSpecial) {
		try {
            MethodHandles.Lookup lookup = createLookup(method.getDeclaringClass());
            MethodHandle methodHandle = invokeSpecial? lookup.unreflectSpecial(method, method.getDeclaringClass()) : lookup.unreflect(method);
            return _create(lambdaInterface, lookup, methodHandle);
        } catch (Throwable e) {
            return XCaught.throwException(e);//can`t return
        }
	}
	
	private static <T> T _create(Class<T> lambdaInterface, MethodHandles.Lookup lookup, MethodHandle methodHandle) throws LambdaConversionException, Throwable {
		MethodType instantiatedMethodType = methodHandle.type();
		MethodType samMethodType = makeMethodTypeGeneric(instantiatedMethodType);
		String signatureName = getNameFromLambdaInterface(lambdaInterface);
		CallSite site = LambdaMetafactory.metafactory(
				lookup,
				signatureName,
				MethodType.methodType(lambdaInterface),
				samMethodType,
				methodHandle,
				instantiatedMethodType);
		return (T) site.getTarget().invoke();
	}

	private static String getNameFromLambdaInterface(Class<?> lambdaInterface) {
		assert lambdaInterface.isInterface();
		return Arrays.stream(lambdaInterface.getMethods()).filter(m->!m.isDefault()&&(m.getModifiers()&Modifier.STATIC)==0).findAny().get().getName();
	}

	/**
	 * change instantiated method type paramters to generic (Object)
	 */
	private static MethodType makeMethodTypeGeneric(MethodType methodType) {
		MethodType sam = methodType;
		Class<?>[] params = sam.parameterArray();
		for (int i = 0; i < params.length; i++) {
			if (Object.class.isAssignableFrom(params[i])){
				sam = sam.changeParameterType(i, Object.class);
			}
		}
		if (Object.class.isAssignableFrom(sam.returnType())){
			sam = sam.changeReturnType(Object.class);
		}
		return sam;
	}
	
	private static Lookup createLookup(Class<?> clazz) throws NoSuchFieldException, IllegalAccessException {
		Lookup lookup = MethodHandles.lookup().in(clazz);
		getLookupsModifiersField().set(lookup, ALL_MODES);
		return lookup;
	}

	static Field getLookupsModifiersField() throws NoSuchFieldException, IllegalAccessException {
		if (lookupAllowedModesField == null || !lookupAllowedModesField.isAccessible()) {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);

			Field allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
			allowedModes.setAccessible(true);
			int modifiers = allowedModes.getModifiers();
			modifiersField.setInt(allowedModes, modifiers & ~Modifier.FINAL); //Remove the final flag
			
			lookupAllowedModesField = allowedModes;
		}
		return lookupAllowedModesField;
	}

}

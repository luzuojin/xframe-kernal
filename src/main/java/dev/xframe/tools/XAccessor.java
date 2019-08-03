package dev.xframe.tools;

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
import java.lang.reflect.Executable;
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
public class XAccessor {
	
	private static Field lookupAllowedModesField;
	private static final int ALL_MODES = (PRIVATE | PROTECTED | PACKAGE | PUBLIC);
	
	@SuppressWarnings("unchecked")
	public static <T> Supplier<T> newConstructorLambda(Class<?> clazz, Class<?>... parameterTypes) throws Throwable {
		Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
		constructor.setAccessible(true);
		MethodHandles.Lookup lookup = newLookup(clazz);
		MethodHandle methodHandle = lookup.unreflectConstructor(constructor);
		return (Supplier<T>) _newLambda(Supplier.class, constructor, lookup, methodHandle);
	}
	
	public static <T> T newLambda(Class<T> lambdaInterface, Class<?> clazz, String methodName, Class<?>... parameterTypes) throws Throwable {
		return newLambda(lambdaInterface, getMethod(clazz, methodName, parameterTypes));
	}
	public static <T> T newLambdaSpecial(Class<T> lambdaInterface, Class<?> clazz, String methodName, Class<?>... parameterTypes) throws Throwable {
		return newLambdaSpecial(lambdaInterface, getMethod(clazz, methodName, parameterTypes));
	}

	private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws Exception {
		Method method = clazz.getMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method;
	}

	public static <T> T newLambda(Class<T> lambdaInterfaceClass, Method method) throws Throwable {
		return _newLambda(lambdaInterfaceClass, method, false);
	}
	public static <T> T newLambdaSpecial(Class<T> lambdaInterface, Method method) throws Throwable {
		return _newLambda(lambdaInterface, method, true);
	}
	
	private static <T> T _newLambda(Class<T> lambdaInterface, Method method, boolean invokeSpecial) throws Throwable {
		MethodHandles.Lookup lookup = newLookup(method.getDeclaringClass());
		MethodHandle methodHandle = invokeSpecial? lookup.unreflectSpecial(method, method.getDeclaringClass()) : lookup.unreflect(method);
		return _newLambda(lambdaInterface, method, lookup, methodHandle);
	}
	
	private static <T> T _newLambda(Class<T> lambdaInterface, Executable method, MethodHandles.Lookup lookup, MethodHandle methodHandle) throws LambdaConversionException, Throwable {
		MethodType instantiatedMethodType = methodHandle.type();
		MethodType signature = newLambdaMethodType(method, instantiatedMethodType);
		String signatureName = getNameFromLambdaInterceClass(lambdaInterface);
		CallSite site = LambdaMetafactory.metafactory(
				lookup, 
				signatureName,
				MethodType.methodType(lambdaInterface), 
				signature, 
				methodHandle, 
				instantiatedMethodType);
		return (T) site.getTarget().invoke();
	}

	private static String getNameFromLambdaInterceClass(Class<?> lambdaInterfaceClass) {
		assert lambdaInterfaceClass.isInterface();
		return Arrays.stream(lambdaInterfaceClass.getMethods()).filter(m->!m.isDefault()&&(m.getModifiers()&Modifier.STATIC)==0).findAny().get().getName();
	}

	private static MethodType newLambdaMethodType(Executable method, MethodType instantiatedMethodType) {
		boolean nullInvokable = Modifier.isStatic(method.getModifiers()) || (method instanceof Constructor);
		MethodType signature = nullInvokable ? instantiatedMethodType : instantiatedMethodType.changeParameterType(0, Object.class);
		Class<?>[] params = method.getParameterTypes();
		for (int i=0; i<params.length; i++){
			if (Object.class.isAssignableFrom(params[i])){
				signature = signature.changeParameterType(nullInvokable ? i : i+1, Object.class);
			}
		}
		if (Object.class.isAssignableFrom(signature.returnType())){
			signature = signature.changeReturnType(Object.class);
		}
		return signature;
	}
	
	private static Lookup newLookup(Class<?> clazz) throws NoSuchFieldException, IllegalAccessException {
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

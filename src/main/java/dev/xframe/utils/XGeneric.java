package dev.xframe.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

public class XGeneric {
	
    final GElement[] elements;
    XGeneric(GElement[] elements) {
        this.elements = elements;
    }
    public Class<?> getByName(String name) {
        for (GElement info : elements) {
            if(info.name.equals(name)) return info.type;
        }
        return null;
    }
    public Class<?> getByType(Class<?> type) {
        for (GElement info : elements) {
            if(type.isAssignableFrom(info.type)) return info.type;
        }
        return null;
    }
    public Class<?> getByIndex(int index) {
    	return elements[index].type;
    }
    public Class<?> getOnlyType() {
        return elements.length == 1 ? elements[0].type : null;
    }
    
    public Class<?>[] getParameterTypes(Method method) {
        return XGeneric.getParamterTypes(this, method);
    }
    
    
    private static String keyName(Class<?> clazz, TypeVariable<?> variable) {
        return clazz.getName() + '@' + variable.getName();
    }
    
    private static XGeneric buildGeneric(Class<?> genericType, Map<String, Class<?>> map) {
        TypeVariable<?>[] typeParameters = genericType.getTypeParameters();
        GElement[] els = new GElement[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            els[i] = new GElement(typeParameters[i].getName(), map.get(keyName(genericType, typeParameters[i])));
        }
        return new XGeneric(els);
    }
    
    /**
     * 获得方法的参数准确类型
     * @param generic
     * @param method
     * @return
     */
    static Class<?>[] getParamterTypes(XGeneric generic, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Class<?>[] ret = new Class<?>[genericParameterTypes.length];
        for (int i = 0; i < ret.length; i++) {
            Type type = genericParameterTypes[i];
            if(type instanceof TypeVariable) {
                TypeVariable<?> tv = (TypeVariable<?>) type;
                Class<?> clazz = generic.getByName(tv.getName());
                if(clazz != null) {
                    ret[i] = clazz;
                    continue;
                }
            }
            ret[i] = parameterTypes[i];
        }
        return ret;
    }

    /**
     * @param type (generic implementation class)
     * @param genericType
     * @return
     */
    public static XGeneric parse(Class<?> type, Class<?> genericType) {
    	if(!genericType.isAssignableFrom(type)) {
    		return new XGeneric(new GElement[0]);
    	}
    	
        if(genericType.isInterface() && type.isSynthetic()) {
            return parseLambda(type, genericType);
        }
        
        return buildGeneric(genericType, parseNormal(type, new HashMap<>()));
    }
    
    private static Map<String, Class<?>> parseNormal(Class<?> clazz, Map<String, Class<?>> map) {
		if(clazz == null || Object.class.equals(clazz)) {
		    return map;
		}
		
		parseGenericType(map, clazz.getGenericSuperclass());
		for (Type genericInterfaze : clazz.getGenericInterfaces()) {
			parseGenericType(map, genericInterfaze);
		}
		
		parseNormal(clazz.getSuperclass(), map);
		for (Class<?> interfaze : clazz.getInterfaces()) {
			parseNormal(interfaze, map);
		}
		return map;
	}

	private static void parseGenericType(Map<String, Class<?>> map, Type generic) {
		if(generic instanceof ParameterizedType) {
			ParameterizedType genericType = (ParameterizedType) generic;
			Class<?> type = (Class<?>) genericType.getRawType();
			
			TypeVariable<?>[] typeParameters = type.getTypeParameters();
			Type[] typeArguments = genericType.getActualTypeArguments();
			
			for (int i = 0; i < typeArguments.length; i++) {
			    map.put(keyName(type, typeParameters[i]), parseTypeArgument(map, typeArguments[i]));
			}
		}
	}
    private static Class<?> parseTypeArgument(Map<String, Class<?>> map, Type typeArgument) {
        if(typeArgument instanceof Class<?>) {
            return (Class<?>) typeArgument;
        } else if(typeArgument instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) typeArgument;
            return map.get(keyName((Class<?>) variable.getGenericDeclaration(), variable));
        } else if(typeArgument instanceof GenericArrayType) {
            return Array.newInstance(parseTypeArgument(map, ((GenericArrayType)typeArgument).getGenericComponentType()), 0).getClass();
        } else if(typeArgument instanceof WildcardType){
            //nothing
        } else if(typeArgument instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType)typeArgument).getRawType();
        }
        return null;
    }
	
    private static XGeneric parseLambda(Class<?> type, Class<?> genericType) {
        try {
            Map<String, Class<?>> map = new HashMap<>();
            
            Class<?> declaringClazz = parseLambda(type, map);
            
            if(!declaringClazz.equals(genericType)) {
                if(genericType.isAssignableFrom(declaringClazz)) {
                    analyseLambdaDownstream(map, declaringClazz);
                } else if(declaringClazz.isAssignableFrom(genericType)) {
                    analyseLambdaUpstream(map, genericType);
                }
            }
            return buildGeneric(genericType, map);
        } catch (Exception e) {
            //ignore
        }
        return null;
    }
    //下层先确定 同正常情况
    private static void analyseLambdaDownstream(Map<String, Class<?>> genericInfos, Class<?> genericType) {
        for (Type ginterfaze : genericType.getGenericInterfaces()) {
            parseGenericType(genericInfos, ginterfaze);
        }
        for (Class<?> interfaze : genericType.getInterfaces()) {
            analyseLambdaDownstream(genericInfos, interfaze);
        }
    }
    //上层先确定
    private static void analyseLambdaUpstream(Map<String, Class<?>> genericInfos, Class<?> genericType) {
        TypeVariable<?>[] params = genericType.getTypeParameters();
        for (TypeVariable<?> param : params) {
            if(genericInfos.containsKey(keyName(genericType, param))) return;
        }
        for (Class<?> interfaze : genericType.getInterfaces()) {
            analyseLambdaUpstream(genericInfos, interfaze);
        }
        for (Type ginterfaze : genericType.getGenericInterfaces()) {
            analyseLambdaUpstream(genericInfos, ginterfaze, genericType);
        }
    }
    private static void analyseLambdaUpstream(Map<String, Class<?>> genericInfos, Type generic, Class<?> impl) {
        if(generic instanceof ParameterizedType) {
            ParameterizedType genericType = (ParameterizedType) generic;
            Class<?> type = (Class<?>) genericType.getRawType();
            
            TypeVariable<?>[] typeParameters = type.getTypeParameters();
            Type[] typeArguments = genericType.getActualTypeArguments();
            
            for (int i = 0; i < typeArguments.length; i++) {
                Type typeArgument = typeArguments[i];
                if(typeArgument instanceof TypeVariable<?>) {
                    TypeVariable<?> variable = (TypeVariable<?>) typeArgument;
                    genericInfos.put(keyName(impl, variable), genericInfos.get(keyName(type, typeParameters[i])));
                }
            }
        }
    }
    
    private static Class<?> parseLambda(Class<?> type, Map<String, Class<?>> map) throws Exception {
        Method lambdaBased = getLambdaBasedMethod(type);
        Method lambdaSuper = getLambdaSuperMethod(type.getInterfaces()[0]);
        Class<?> superClazz = lambdaSuper.getDeclaringClass();
        
        if(lambdaSuper.getGenericReturnType() instanceof TypeVariable<?>) {
            map.put(keyName(superClazz, (TypeVariable<?>) lambdaSuper.getGenericReturnType()), lambdaBased.getReturnType());
        }
        
        Class<?>[] bpcs = lambdaBased.getParameterTypes();
        Type[] spcs = lambdaSuper.getGenericParameterTypes();
        int offset = bpcs.length - spcs.length;//前面参数是lambda携带的局部变量(?)
        for (int i = 0; i < spcs.length; i++) {
            if(spcs[i] instanceof TypeVariable<?>) {
                map.put(keyName(superClazz, (TypeVariable<?>) spcs[i]), bpcs[i+offset]);
            }
        }
        return superClazz;
    }
    
    private static Method poolObjGetter;
    private static Method poolSizeGetter;
    private static Method poolMethodGetter;
    static {
        try {
            poolObjGetter = getAccessibleMethod(Class.class, "getConstantPool");
            Class<?> poolClass = poolObjGetter.invoke(Class.class).getClass();
            poolSizeGetter = getAccessibleMethod(poolClass, "getSize");
            poolMethodGetter = getAccessibleMethod(poolClass, "getMethodAt", int.class);
        } catch (Exception e) {}//ignore
    }
    private static Method getAccessibleMethod(Class<?> clazz, String name, Class<?>... params) throws Exception {
        Method method = clazz.getDeclaredMethod(name, params);
        method.setAccessible(true);
        return method;
    }
    private static Method getLambdaBasedMethod(Class<?> lambdaType) throws Exception {
        Object pool = poolObjGetter.invoke(lambdaType);
        int size = (Integer) poolSizeGetter.invoke(pool);
        for (int i = 0; i < size; i++) {
            Member member = poolMethodAt(pool, i);
            if(member != null && !(member instanceof Constructor<?>) && !member.getDeclaringClass().isAssignableFrom(lambdaType)) {
                return (Method) member;
            }
        }
        return null;
    }
    private static Member poolMethodAt(Object pool, int i) {
        try {
            return (Member) poolMethodGetter.invoke(pool, i);
        } catch (Exception e) {}//ignore
        return null;
    }
    private static Method getLambdaSuperMethod(Class<?> type) {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if(!method.isDefault() && !Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        }
        return null;
    }
    public static class GElement {
        public final String name;
        public final Class<?> type;
        public GElement(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
        @Override
        public String toString() {
            return name + " : " + type.getName();
        }
    }
    
}
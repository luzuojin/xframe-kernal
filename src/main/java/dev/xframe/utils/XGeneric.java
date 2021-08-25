package dev.xframe.utils;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
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
    private final GVariable[] variables;
    private XGeneric(GVariable[] variables) {
        this.variables = variables;
    }
    public Class<?> getByName(String name) {
        for (GVariable var : variables) {
            if(var.name.equals(name)) return var.type;
        }
        return null;
    }
    public Class<?> getByType(Class<?> type) {
        for (GVariable var : variables) {
            if(type.isAssignableFrom(var.type)) return var.type;
        }
        return null;
    }
    /**
     * [0,len)
     */
    public Class<?> getByIndex(int index) {
    	return variables[index].type;
    }
    public Class<?> getOnlyType() {
        return variables.length == 1 ? variables[0].type : null;
    }
    public Class<?>[] getParameterTypes(Method method) {
        return XGeneric.getParamterTypes(this, method);
    }
    
    
    private static String keyName(Class<?> clazz, TypeVariable<?> variable) {
        return clazz.getName() + '@' + variable.getName();
    }
    
    private static XGeneric makeGeneric(Class<?> genericType, Map<String, Class<?>> map) {
        TypeVariable<?>[] typeParameters = genericType.getTypeParameters();
        GVariable[] vars = new GVariable[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            vars[i] = new GVariable(typeParameters[i].getName(), map.get(keyName(genericType, typeParameters[i])));
        }
        return new XGeneric(vars);
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
    		return new XGeneric(new GVariable[0]);
    	}
    	
        if(genericType.isInterface() && type.isSynthetic()) {
            return parseLambda(type, genericType);
        }
        
        return parseNormal(type, genericType);
    }
    
    private static XGeneric parseNormal(Class<?> type, Class<?> genericType) {
    	return makeGeneric(genericType, parseNormal(type, new HashMap<>()));
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
                    parseLambdaDownstream(map, declaringClazz);
                } else if(declaringClazz.isAssignableFrom(genericType)) {
                    parseLambdaUpstream(map, genericType);
                }
            }
            return makeGeneric(genericType, map);
        } catch (Exception e) {//ignore
        	e.printStackTrace();
        }
        return null;
    }
    //下层先确定 同正常情况
    private static void parseLambdaDownstream(Map<String, Class<?>> genericInfos, Class<?> genericType) {
        for (Type ginterfaze : genericType.getGenericInterfaces()) {
            parseGenericType(genericInfos, ginterfaze);
        }
        for (Class<?> interfaze : genericType.getInterfaces()) {
            parseLambdaDownstream(genericInfos, interfaze);
        }
    }
    //上层先确定
    private static void parseLambdaUpstream(Map<String, Class<?>> genericInfos, Class<?> genericType) {
        TypeVariable<?>[] params = genericType.getTypeParameters();
        for (TypeVariable<?> param : params) {
            if(genericInfos.containsKey(keyName(genericType, param))) return;
        }
        for (Class<?> interfaze : genericType.getInterfaces()) {
            parseLambdaUpstream(genericInfos, interfaze);
        }
        for (Type ginterfaze : genericType.getGenericInterfaces()) {
            parseLambdaUpstream(genericInfos, ginterfaze, genericType);
        }
    }
    private static void parseLambdaUpstream(Map<String, Class<?>> genericInfos, Type generic, Class<?> impl) {
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
        Member lambdaImple = getLambdaBasedMethod(type);//method or constructor
        Method lambdaSuper = getLambdaSuperMethod(type.getInterfaces()[0]);
        Class<?> superClazz = lambdaSuper.getDeclaringClass();
        
        if(lambdaSuper.getGenericReturnType() instanceof TypeVariable<?>) {
            Class<?> returnType = lambdaImple instanceof Method ? ((Method) lambdaImple).getReturnType()
            		: ((Constructor<?>) lambdaImple).getDeclaringClass();
			map.put(keyName(superClazz, (TypeVariable<?>) lambdaSuper.getGenericReturnType()), XBoxing.getWrapper(returnType));
        }
        
        Class<?>[] impleParamters = ((Executable) lambdaImple).getParameterTypes();
        Type[] superParamters = lambdaSuper.getGenericParameterTypes();
        //第一个参数为methodRef对象本身的lambda表达式
        int superOffset = 0;//BiFunction<String, Integer, Character> = String::charAt;
        if(superParamters.length > 0 && superParamters[0] instanceof TypeVariable 
        		&& superParamters.length == impleParamters.length + 1) {
        	map.put(keyName(superClazz, (TypeVariable<?>) superParamters[0]), lambdaImple.getDeclaringClass());
        	superOffset = 1;
        }
        //superOffset>0时impleParamters<superParamters
        //lambda表达式中带有局部(enclosing)变量
        int impleOffset = Math.max(0, impleParamters.length - superParamters.length);
        for (int i = 0; i + superOffset < superParamters.length; i++) {
            if(superParamters[i] instanceof TypeVariable) {
                map.put(keyName(superClazz, (TypeVariable<?>) superParamters[i+superOffset]), XBoxing.getWrapper(impleParamters[i+impleOffset]));
            }
        }
        return superClazz;
    }
    
    private static Method poolObjGetter;
    private static Method poolSizeGetter;
    private static Method poolMethodGetter;
    static {
        try {
            poolObjGetter = XReflection.getMethod(Class.class, "getConstantPool");
            Class<?> poolClass = poolObjGetter.invoke(Class.class).getClass();
            poolSizeGetter = XReflection.getMethod(poolClass, "getSize");
            poolMethodGetter = XReflection.getMethod(poolClass, "getMethodAt", int.class);
        } catch (Exception e) {e.printStackTrace();}//ignore
    }
    private static Member getLambdaBasedMethod(Class<?> lambdaType) throws Exception {
        Object pool = poolObjGetter.invoke(lambdaType);
        int size = (Integer) poolSizeGetter.invoke(pool);
        for (int i = 0; i < size; i++) {
            Member member = poolMethodAt(pool, i);
            if(member == null || isLambdaInternal(member, lambdaType)) {
            	continue;
            }
            if(!(member instanceof Method && XBoxing.isBoxingMethod((Method) member))) {
            	return member;
            }
        }
        return null;
    }
    private static boolean isLambdaInternal(Member member, Class<?> lambdaType) {
    	Class<?> declaringClass = member.getDeclaringClass();
		return declaringClass == lambdaType || (member instanceof Constructor && (declaringClass == Object.class || declaringClass == SerializedLambda.class));
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
    public static class GVariable {
        public final String name;
        public final Class<?> type;
        public GVariable(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
        @Override
        public String toString() {
            return name + " : " + type.getName();
        }
    }
    
}
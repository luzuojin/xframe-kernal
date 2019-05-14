package dev.xframe.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import dev.xframe.tools.Reflection;

/**
 * listeners with @Subscribe declared by class 
 * @author luzj
 */
public final class Registry {
	
	public static void regist(Object declaredObj, Registrator registrator) {
        regist(declaredObj.getClass(), declaredObj, registrator);
    }
    public static void regist(Class<?> declared, Object declaredObj, Registrator registrator) {
        Declaring[] declaring = get(declared);
        for (Declaring r : declaring) {
            registrator.regist(new DeclaringSubscriber(r.group, r.type, declaredObj, r.method));
        }
    }
    
	static class Declaring {
	    final int group;
	    final int type;
	    final Method method;
	    
	    public Declaring(int group, Method m) {
	        this.group = group;
	        this.type = checkAndGetEventType(m);
	        this.method = m;
	        this.method.setAccessible(true);
	    }
	    
		private int checkAndGetEventType(Method m) {
			if(m.getParameterTypes().length != 1 || m.getParameterTypes()[0].getAnnotation(Event.class) == null) {
				throw new IllegalArgumentException("Event subscribe method must has only one paramter with @Event marked)");
			}
			return m.getParameterTypes()[0].getAnnotation(Event.class).value();
		}
	}
    
    private static AtomicInteger _group = new AtomicInteger();
    private static Map<Class<?>, Integer> _groups = new HashMap<>();
    public static int getGroup(Class<?> declared) {
        Integer group = _groups.get(declared);
        return group == null ? getGroupSafely(declared) : group.intValue();
    }
    public static int getGroup() {
        return getGroup(Reflection.getCallerClass());
    }
    
    private static synchronized int getGroupSafely(Class<?> declared) {
        Integer group = _groups.get(declared);
        if(group != null){
            return group.intValue();
        }
        
        int g = _group.incrementAndGet();
        _groups.put(declared, g);
        return g;
    }

    private static Map<Class<?>, Declaring[]> declarings = new HashMap<>();
    private static Declaring[] get(Class<?> declared) {
        Declaring[] r = declarings.get(declared);
        return r == null ? getSafely(declared) : r;
    }

    private static synchronized Declaring[] getSafely(Class<?> declared) {
        Declaring[] r = declarings.get(declared);
        if(r == null) {
            r = build(declared);
            declarings.put(declared, r);
        }
        return r;
    }
    
    public static void unregist(Class<?> declared, Registrator registrator) {
        registrator.unregist(getGroup(declared));
    }

    private static Declaring[] build(Class<?> declared) {
        int group = getGroup(declared);
        return Arrays.stream(declared.getMethods())
                .filter(m -> m.isAnnotationPresent(Subscribe.class) && !Void.class.isAssignableFrom(getOnlyParamType(m)))
                .map(m -> new Declaring(group, m))
                .toArray(s -> new Declaring[s]);
    }

    static class DeclaringSubscriber extends Subscriber {
    	final Object declareObj;
    	final Method method;
    	public DeclaringSubscriber(int group, int type, Object declare, Method method) {
    		super(group, type);
    		this.declareObj = declare;
    		this.method = method;
    	}
		@Override
		public void onEvent(Object event) throws Exception {
			method.invoke(declareObj, event);
		}
    }
    
    private static Class<?> getOnlyParamType(Method method) {
        Class<?>[] types = method.getParameterTypes();
        return types.length == 1 ? types[0] : Void.class;
    }

}

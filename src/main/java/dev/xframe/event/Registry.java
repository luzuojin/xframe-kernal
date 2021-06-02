package dev.xframe.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import dev.xframe.utils.XReflection;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XLambda;

/**
 * listeners with @Subscribe declared by class 
 * @author luzj
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class Registry {
	
	public static void regist(Object declaredObj, Registrator registrator) {
        regist(declaredObj.getClass(), declaredObj, registrator);
    }
    public static void regist(Class<?> declared, Object declaredObj, Registrator registrator) {
        Declaring[] declaring = get(declared);
        for (Declaring r : declaring) {
            registrator.regist(r.newSubscriber(declaredObj));
        }
    }
    
	static class Declaring {
	    final int group;
	    final int type;
	    final BiConsumer invoker;
	    
		public Declaring(int group, Method m) {
            try {
				XReflection.setAccessible(m);
				this.group = group;
				this.type = checkAndGetEventType(m);
				this.invoker = XLambda.create(BiConsumer.class, m);
			} catch (Throwable e) {
				throw XCaught.throwException(e);
			}
	    }
		private int checkAndGetEventType(Method m) {
			if(m.getParameterTypes().length != 1 || m.getParameterTypes()[0].getAnnotation(Event.class) == null) {
				throw new IllegalArgumentException("Event subscribe method must has only one paramter with @Event marked)");
			}
			return m.getParameterTypes()[0].getAnnotation(Event.class).value();
		}
		
		public Subscriber newSubscriber(Object declaredObj) {
			return new DeclaringSubscriber(this, declaredObj);
		}
	}
	
	static class DeclaringSubscriber extends Subscriber {
    	final Object declareObj;
		final BiConsumer invoker;
    	public DeclaringSubscriber(Declaring declaring, Object declareObj) {
    		super(declaring.group, declaring.type);
    		this.declareObj = declareObj;
    		this.invoker = declaring.invoker;
    	}
		@Override
		public void onEvent(Object event) throws Exception {
			invoker.accept(declareObj, event);
		}
    }
    
    private static AtomicInteger _group = new AtomicInteger();
    private static Map<Class<?>, Integer> _groups = new HashMap<>();
    public static int getGroup(Class<?> declared) {
        Integer group = _groups.get(declared);
        return group == null ? getGroupSafely(declared) : group.intValue();
    }
    public static int getGroup() {
        return getGroup(XReflection.getCallerClass());
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

    private static Class<?> getOnlyParamType(Method method) {
        Class<?>[] types = method.getParameterTypes();
        return types.length == 1 ? types[0] : Void.class;
    }

}

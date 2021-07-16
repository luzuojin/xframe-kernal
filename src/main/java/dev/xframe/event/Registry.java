package dev.xframe.event;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import dev.xframe.utils.XCaught;
import dev.xframe.utils.XLambda;
import dev.xframe.utils.XReflection;

/**
 * listeners with @Subscribe delegated by class 
 * @author luzj
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class Registry {

    public static void regist(Object delegateCls, Registrator registrator) {
        regist(delegateCls.getClass(), delegateCls, registrator);
    }
    public static void regist(Class<?> delegateCls, Object delegateObj, Registrator registrator) {
        Delegation[] delegation = get(delegateCls);
        for (Delegation d : delegation) {
            registrator.regist(d.newSubscriber(delegateObj));
        }
    }

    static class Delegation {
        final int group;
        final int type;
        final BiConsumer invoker;

        public Delegation(int group, Method m) {
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

        public Subscriber newSubscriber(Object delegateObj) {
            return new DelegationSubscriber(this, delegateObj);
        }
    }

    static class DelegationSubscriber extends Subscriber {
        final Object delegateObj;
        final BiConsumer invoker;
        public DelegationSubscriber(Delegation delegateion, Object deletageObj) {
            super(delegateion.group, delegateion.type);
            this.delegateObj = deletageObj;
            this.invoker = delegateion.invoker;
        }
        @Override
        public void onEvent(Object event) throws Exception {
            invoker.accept(delegateObj, event);
        }
    }

    private static AtomicInteger _group = new AtomicInteger();
    private static Map<Class<?>, Integer> _groups = new HashMap<>();
    public static int getGroup(Class<?> cls) {
        Integer group = _groups.get(cls);
        return group == null ? getGroupSafely(cls) : group.intValue();
    }
    public static int getGroup() {
        return getGroup(XReflection.getCallerClass());
    }

    private static synchronized int getGroupSafely(Class<?> cls) {
        Integer group = _groups.get(cls);
        if(group != null){
            return group.intValue();
        }

        int g = _group.incrementAndGet();
        _groups.put(cls, g);
        return g;
    }

    private static Map<Class<?>, Delegation[]> delegations = new HashMap<>();
    private static Delegation[] get(Class<?> cls) {
        Delegation[] r = delegations.get(cls);
        return r == null ? getSafely(cls) : r;
    }

    private static synchronized Delegation[] getSafely(Class<?> cls) {
        Delegation[] r = delegations.get(cls);
        if(r == null) {
            r = build(cls);
            delegations.put(cls, r);
        }
        return r;
    }

    public static void unregist(Class<?> delegateCls, Registrator registrator) {
        registrator.unregist(getGroup(delegateCls));
    }

    private static Delegation[] build(Class<?> cls) {
        int group = getGroup(cls);
        return Arrays.stream(cls.getMethods())
                .filter(m -> m.isAnnotationPresent(Subscribe.class) && !Void.class.isAssignableFrom(getOnlyParamType(m)))
                .map(m -> new Delegation(group, m))
                .toArray(s -> new Delegation[s]);
    }

    private static Class<?> getOnlyParamType(Method method) {
        Class<?>[] types = method.getParameterTypes();
        return types.length == 1 ? types[0] : Void.class;
    }

}

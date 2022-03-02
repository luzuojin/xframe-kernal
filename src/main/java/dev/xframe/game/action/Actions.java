package dev.xframe.game.action;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.code.Codes;
import dev.xframe.net.cmd.Cmd;
import dev.xframe.utils.XGeneric;
import dev.xframe.utils.XReflection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("ALL")
@Bean
public class Actions implements Loadable {

    //key, Action.M
    private static final Map<Class<?>, ActionFactory> MsgBasedMap = new HashMap<>();
    //key, Action.class or Runnable.class
    private static final Map<Class<?>, ActionFactory> ClsBasedMap = new HashMap<>();

    @Inject
    private ActionInjectorFactory injectorFactory;

    @Override
    public void load() {
        Codes.getScannedClasses().stream()
                .filter(Action.class::isAssignableFrom)
                .filter(XReflection::isImplementation)
                .filter(Predicate.isEqual(RunnableAction.class).negate())
                .forEach(this::makeFactory);
    }

    ActionFactory makeFactory0(Class<?> cls) {
        return new ActionFactory(cls, injectorFactory.make(cls));
    }

    void makeFactory(Class<?> cls) {
        Class<?> msg = XGeneric.parse(cls, Action.class).getByIndex(1);
        ActionFactory fac = makeFactory0(cls);
        //非cmd action 消息(Msg)不能重复, cmd action使用code区别所以可以重复.
        if(!cls.isAnnotationPresent(Cmd.class)) {
            ActionFactory ex = MsgBasedMap.put(msg, fac);
            if(ex != null)
                throw new IllegalStateException(String.format("Conflict msg[%s] int actions[%s, %s]", msg, cls, ex.cls));
        }
        ClsBasedMap.put(cls, fac);
    }

    public static ActionFactory getFactoryByCls(Class<?> cls) {
        return ClsBasedMap.get(cls);
    }
    public static ActionFactory getFactoryByMsg(Class<?> msg) {
        return MsgBasedMap.get(msg);
    }

    public static <T extends Actor, M> Action<T, M> makeByMsg(Actor actor, Object msg) {
        return getFactoryByMsg(msg.getClass()).make(actor);
    }
    public static <T extends Actor, M> Action<T, M> makeByCls(Actor actor, Class<?> cls) {
        return getFactoryByCls(cls).make(actor);
    }
    public static <T extends Actor, M> Action<T, M> makeByRunnable(Actor actor, Runnable<T> r) {
        return new RunnableAction();
    }

}

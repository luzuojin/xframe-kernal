package dev.xframe.game.action;

import dev.xframe.game.config.GameConfigurator;
import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Dependence;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.Codes;
import dev.xframe.net.cmd.Cmd;
import dev.xframe.utils.XFactory;
import dev.xframe.utils.XGeneric;
import dev.xframe.utils.XReflection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("ALL")
@Configurator
@Dependence(GameConfigurator.class)
public class Actions implements Loadable {

    //key, Action.M
    private static final Map<Class<?>, ClsActionFactory> MsgBasedMap = new HashMap<>();
    //key, Action.class or Runnable.class
    private static final Map<Class<?>, ClsActionFactory> ClsBasedMap = new HashMap<>();

    private static Function<Class<?>, ClsActionFactory> FactoryFunc;

    @Inject
    private ModularAdapter mAdapter;

    @Override
    public void load() {
        FactoryFunc = this::makeFactory0;
        Codes.getScannedClasses().stream()
                .filter(Action.class::isAssignableFrom)
                .filter(XReflection::isImplementation)
                .forEach(Actions::makeFactory);
    }

    ClsActionFactory makeFactory0(Class<?> cls) {
        final XFactory<?> fac = XFactory.of(cls);
        final ModularAdapter adapter = this.mAdapter;
        final Injector injector = adapter.newInjector(cls);
        if(Runnable.class.isAssignableFrom(cls)) {
            return new ClsActionFactory(cls) {
                @Override
                public <T extends Player, M> Action<T, M> make(Player player) {
                    return new RunnableAction();
                }
            };
        } else if(ModularAction.class.isAssignableFrom(cls)) {
            final Class<?> mCls = XGeneric.parse(cls, ModularAction.class).getByIndex(1);
            final MTypedLoader mtLoader = adapter.getTypedLoader(mCls);
            return new ClsActionFactory(cls) {
                @Override
                public <T extends Player, M> Action<T, M> make(Player player) {
                    Action<T, M> action = (Action<T, M>) fac.get();
                    ((ModularAction<?, ?, ?>) action).mTyped = mtLoader;
                    adapter.runInject(injector, action, player);
                    return action;
                }
            };
        } else {
            return new ClsActionFactory(cls) {
                @Override
                public <T extends Player, M> Action<T, M> make(Player player) {
                    Action<T, M> action = (Action<T, M>) fac.get();
                    adapter.runInject(injector, action, player);
                    return action;
                }
            };
        }
    }

    static void makeFactory(Class<?> cls) {
        Class<?> msg = XGeneric.parse(cls, Action.class).getByIndex(1);
        ClsActionFactory fac = FactoryFunc.apply(cls);
        //非cmd action 消息(Msg)不能重复, cmd action使用code区别所以可以重复.
        if(!cls.isAnnotationPresent(Cmd.class)) {
            ClsActionFactory ex = MsgBasedMap.put(msg, fac);
            if(ex != null)
                throw new IllegalStateException(String.format("Conflict msg[%] int actions[%s, %s]", msg, cls, ex.cls));
        }
        ClsBasedMap.put(cls, fac);
    }

    public static ActionFactory getFactoryByCls(Class<?> cls) {
        return ClsBasedMap.get(cls);
    }
    public static ActionFactory getFactoryByMsg(Class<?> msg) {
        return MsgBasedMap.get(msg);
    }

    public static <T extends Player, M> Action<T, M> makeByMsg(Player player, Object msg) {
        return getFactoryByMsg(msg.getClass()).make(player);
    }
    public static <T extends Player, M> Action<T, M> makeByCls(Player player, Class<?> cls) {
        return getFactoryByCls(cls).make(player);
    }
    public static <T extends Player, M> Action<T, M> makeByRunnable(Player player, Runnable<T> runnable) {
        return new RunnableAction();
    }

    static abstract class ClsActionFactory implements ActionFactory {
        final Class<?> cls;
        public ClsActionFactory(Class<?> cls) {
            this.cls = cls;
        }
    }

}

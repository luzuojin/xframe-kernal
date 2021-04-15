package dev.xframe.game.callable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.beans.Injector;
import dev.xframe.utils.XGeneric;

@Prototype
class CallableInjector {
    @Inject
    private ModularAdapter adapter;
    //object inject
    private Injector injector;
    //param inject
    private MTypedLoader loader;
    
    public CallableInjector set(Class<?> type, Class<?> moduleType) {
        injector = adapter.newInjector(type);
        if(moduleType != null) {
            loader = adapter.getTypedLoader(moduleType);
        }
        return this;
    }
    
    public <T> T apply(Player player, Object bean) {
        adapter.runInject(injector, bean, player);
        return loader == null ? null : loader.load(player);
    }
    
    
    static final Map<Class<?>, CallableInjector> cached = new HashMap<>();
    
    static CallableInjector get(Class<?> type) {
        CallableInjector ex = cached.get(type);
        if(ex == null) {
            ex = new CallableInjector().set(type, null);
            cached.put(type, ex);
        }
        return ex;
    }
    static CallableInjector get(Class<?> type, Function<Class<?>, Class<?>> moduleType) {
        CallableInjector ex = cached.get(type);
        if(ex == null) {
            ex = new CallableInjector().set(type, moduleType.apply(type));
            cached.put(type, ex);
        }
        return ex;
    }
    
    //for PlayerCallable
    static <T extends Player> T doInject(T player, Object bean) {
        get(bean.getClass()).apply(player, bean);
        return player;
    }
    
    //for ModularCallable
    static <V, T extends Player> V doInjectAndGetModule(T player, Object bean) {
        return get(bean.getClass(), CallableInjector::getModuleType).apply(player, bean);
    }
    
    static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, ModularCallable.class).getByName("V");
    }

}

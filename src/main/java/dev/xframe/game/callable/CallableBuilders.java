package dev.xframe.game.callable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import dev.xframe.game.player.Player;
import dev.xframe.utils.XGeneric;

class CallableBuilders {
    
    static final Map<Class<?>, CallableBuilder> builders = new HashMap<>();
    
    static CallableBuilder get(Class<?> type) {
        CallableBuilder ex = builders.get(type);
        if(ex == null) {
            ex = new CallableBuilder().setup(type, null);
            builders.put(type, ex);
        }
        return ex;
    }
    static CallableBuilder get(Class<?> type, Function<Class<?>, Class<?>> moduleType) {
        CallableBuilder ex = builders.get(type);
        if(ex == null) {
            ex = new CallableBuilder().setup(type, moduleType.apply(type));
            builders.put(type, ex);
        }
        return ex;
    }
    
    //for PlayerCallable
    static <T extends Player> T setup0(T player, Object bean) {
        get(bean.getClass()).apply(player, bean);
        return player;
    }
    
    //for ModularCallable
    static <V, T extends Player> V setup1(T player, Object bean) {
        return get(bean.getClass(), CallableBuilders::getModuleType).apply(player, bean);
    }
    
    static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, ModularCallable.class).getByName("V");
    }
    
}

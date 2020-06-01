package dev.xframe.game.callable;

import java.util.HashMap;
import java.util.Map;

import dev.xframe.game.player.Player;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.module.ModularContext;
import dev.xframe.module.ModuleTypeLoader;
import dev.xframe.utils.XGeneric;

/**
 * 
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
public interface ModularCallable<T extends Player, V> extends PlayerCallable<T> {
	
	static Map<Class<?>, ModuleTypeLoader> loaders = new HashMap<>();
	
	static ModuleTypeLoader getLoader(Class<?> clazz) {
		ModuleTypeLoader loader = loaders.get(clazz);
		if(loader == null) {
			loader = ApplicationContext.fetchBean(ModularContext.class).getModuleLoader(getModuleType(clazz));
			loaders.put(clazz, loader);
		}
		return loader;
	}

    static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, ModularCallable.class).getByName("V");
    }
    
    default void call(T player) {
        call(player, getLoader(this.getClass()).<V>load(player));
    }
    
    public void call(T player, V module);

}

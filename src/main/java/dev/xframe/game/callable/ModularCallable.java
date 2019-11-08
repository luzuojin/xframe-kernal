package dev.xframe.game.callable;

import java.util.HashMap;
import java.util.Map;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.module.ModularConext;
import dev.xframe.module.ModuleTypeLoader;
import dev.xframe.utils.Generic;

/**
 * 
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
public interface ModularCallable<T extends ModularPlayer, V> extends PlayerCallable<T> {
	
	static Map<Class<?>, ModuleTypeLoader> loaders = new HashMap<>();
	
	static ModuleTypeLoader getLoader(Class<?> clazz) {
		ModuleTypeLoader loader = loaders.get(clazz);
		if(loader == null) {
			loader = ModularConext.getLoader(getModuleType(clazz));
			loaders.put(clazz, loader);
		}
		return loader;
	}

    static Class<?> getModuleType(Class<?> clazz) {
        return Generic.parse(clazz, ModularCallable.class).getByName("V");
    }
    
    default void call(T player) {
        call(player, getLoader(this.getClass()).<V>load(player));
    }
    
    public void call(T player, V module);

}

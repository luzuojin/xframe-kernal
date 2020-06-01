package dev.xframe.game.callable;

import java.util.HashMap;
import java.util.Map;

import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.Player;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.utils.XGeneric;

/**
 * 
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
public interface ModularCallable<T extends Player, V> extends PlayerCallable<T> {
	
	static Map<Class<?>, MTypedLoader> loaders = new HashMap<>();
	
	static MTypedLoader getLoader(Class<?> clazz) {
		MTypedLoader loader = loaders.get(clazz);
		if(loader == null) {
			loader = ApplicationContext.fetchBean(ModularAdapter.class).getTypedLoader(getModuleType(clazz));
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

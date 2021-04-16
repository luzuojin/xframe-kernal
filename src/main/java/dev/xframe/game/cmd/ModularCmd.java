package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XGeneric;

/**
 * module 入口
 * @author luzj
 * @param <T>
 * @param <V>
 */
public abstract class ModularCmd<T extends Player, V> extends PlayerCmd<T> {
	
	@Inject
	private ModularAdapter adapter;
	
    private MTypedLoader loader;
    
    private MTypedLoader getLoader() {
    	if(loader == null)
    		loader = adapter.getTypedLoader(getModuleType(this.getClass()));
		return loader;
	}
    
    public static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, ModularCmd.class).getByName("V");
    }

    public final void exec(T player, IMessage req) throws Exception {
        exec(player, getLoader().load(player), req);
    }
    
	public abstract void exec(T player, V module, IMessage req) throws Exception;
    
}

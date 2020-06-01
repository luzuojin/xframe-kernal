package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.module.ModularContext;
import dev.xframe.module.ModuleTypeLoader;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XGeneric;

/**
 * module 入口
 * @author luzj
 * @param <T>
 * @param <V>
 */
public abstract class ModularCommand<T extends Player, V> extends PlayerCommand<T> {
	
	@Inject
	private ModularContext modularCtx;
	
    private ModuleTypeLoader loader;
    
    private ModuleTypeLoader getLoader() {
    	if(loader == null)
    		loader = modularCtx.getModuleLoader(getModuleType(this.getClass()));
		return loader;
	}
    
    public static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, ModularCommand.class).getByName("V");
    }

    public final void exec(T player, IMessage req) throws Exception {
        exec(player, getLoader().load(player), req);
    }
    
	public abstract void exec(T player, V module, IMessage req) throws Exception;
    
}

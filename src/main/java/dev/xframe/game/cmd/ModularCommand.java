package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.modular.ModularEnigne;
import dev.xframe.modular.ModuleTypeLoader;
import dev.xframe.net.codec.IMessage;
import dev.xframe.tools.Generic;

/**
 * module 入口
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
public abstract class ModularCommand<T extends ModularPlayer, V> extends PlayerCommand<T> {
    
    final ModuleTypeLoader loader = ModularEnigne.getLoader(getModuleType());

    public final Class<?> getModuleType() {
        return Generic.parse(this.getClass(), ModularCommand.class).getByName("V");
    }

    public final void exec(T player, IMessage req) throws Exception {
        exec(player, loader.load(player), req);
    }
    
    public abstract void exec(T player, V module, IMessage req) throws Exception;
    
}

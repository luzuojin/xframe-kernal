package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.modular.ModularBridge;
import dev.xframe.net.codec.IMessage;

/**
 * module 入口
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
@ModularBridge
public abstract class ModularCommand<T extends ModularPlayer, V> extends PlayerCommand<T> {

    @ModularBridge.Source
    public void exec(@ModularBridge.Bridging T player, IMessage req) throws Exception {
        //for dynamic override
        //bridge to Descriptor method
    }
    
    @ModularBridge.Dest
    public abstract void exec(T player, @ModularBridge.Bridging V module, IMessage req) throws Exception;
    
}

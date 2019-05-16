package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.modular.ModularBridge;
import dev.xframe.net.codec.IMessage;

@ModularBridge
public abstract class PlayerCmdAction<T extends ModularPlayer, V> {

    @ModularBridge.Source
    public final void exec(@ModularBridge.Bridging T player, IMessage req) throws Exception {
        // for dynamic override
        // bridge to Descriptor method
    }

    @ModularBridge.Dest
    public abstract void exec(T player, @ModularBridge.Bridging V module, IMessage req) throws Exception;

}

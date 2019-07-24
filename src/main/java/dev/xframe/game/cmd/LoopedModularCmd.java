package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.injection.Inject;
import dev.xframe.net.codec.IMessage;


/**
 * 通过player.loop调用
 * @author luzj
 *
 */
public abstract class LoopedModularCmd<T extends ModularPlayer, V> extends ModularCommand<T, V> {

    @Inject
    private PlayerCmdInvoker<T> invoker;
    
    @Override
    protected final void execute0(T player, IMessage req) throws Exception {
        new PlayerCmdInvokeAction<>(invoker, this, player, req, player.loop()).checkin();
    }
    
}

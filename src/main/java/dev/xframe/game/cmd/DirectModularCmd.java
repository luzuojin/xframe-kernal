package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.injection.Inject;
import dev.xframe.net.codec.IMessage;

/**
 * 直接的, 没有经过player.queue
 * @author luzj
 */
public abstract class DirectModularCmd<T extends ModularPlayer, V> extends ModularCommand<T, V> {
    
    @Inject
    private PlayerCmdInvoker<T> invoker;

    @Override
    protected final void execute0(T player, IMessage req) throws Exception {
        invoker.invoke(this, player, req);
    }
    
}

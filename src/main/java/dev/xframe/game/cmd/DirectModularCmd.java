package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.net.codec.IMessage;

/**
 * 直接的, 没有经过player.loop
 * @author luzj
 */
public abstract class DirectModularCmd<T extends Player, V> extends ModularCommand<T, V> {
    
    @Inject
    private PlayerCmdInvoker<T> invoker;

    @Override
    protected final void execute0(T player, IMessage req) throws Exception {
        invoker.invoke(this, player, req);
    }
    
}

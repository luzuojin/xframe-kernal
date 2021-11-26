package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;

/**
 * 直接的, 没有经过player.loop
 * @author luzj
 */
public abstract class DirectCmd<T extends Player, M> extends PlayerCmd<T, M> {
    
    @Inject
    private PlayerCmdInvoker<T, M> invoker;
    
    @Override
    protected final void execute0(T player, M msg) throws Exception {
        invoker.invoke(this, player, msg);
    }
    
}

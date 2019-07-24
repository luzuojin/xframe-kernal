package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.injection.Inject;
import dev.xframe.net.codec.IMessage;

/**
 * 直接的, 没有经过player.loop
 * @author luzj
 */
public abstract class DirectCommand<T extends Player> extends PlayerCommand<T> {
    
    @Inject
    private PlayerCmdInvoker<T> invoker;
    
    @Override
    protected final void execute0(T player, IMessage req) throws Exception {
        invoker.invoke(this, player, req);
    }
    
}

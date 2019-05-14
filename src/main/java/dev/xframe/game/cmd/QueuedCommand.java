package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;


/**
 * 通过player.queue调用
 * @author luzj
 *
 */
public abstract class QueuedCommand<T extends Player> extends PlayerCommand<T> {
    
    @Override
    protected final void execute0(T player, IMessage req) throws Exception {
        new PlayerCmdInvokeAction<>(this, player, req, player.queue()).checkin();
    }

}

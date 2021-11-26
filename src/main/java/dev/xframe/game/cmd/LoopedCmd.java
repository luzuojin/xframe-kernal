package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;


/**
 * 通过player.loop调用
 * @author luzj
 *
 */
public abstract class LoopedCmd<T extends Player, M> extends PlayerCmd<T, M> {
    
    @Inject
    private PlayerCmdInvoker<T, M> invoker;
    
    @Override
    protected final void execute0(T player, M msg) throws Exception {
        new PlayerCmdInvokeTask<>(invoker, this, player, msg, player.loop()).checkin();
    }

}

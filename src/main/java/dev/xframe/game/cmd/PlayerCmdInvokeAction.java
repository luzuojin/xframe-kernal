package dev.xframe.game.cmd;

import dev.xframe.action.Action;
import dev.xframe.action.ActionQueue;
import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;

/**
 * 通过player task queue 执行的 request command
 * @author luzj
 */
final class PlayerCmdInvokeAction<T extends Player> extends Action {
    
    private PlayerCmdInvoker<T> invoker;
    
    private PlayerCommand<T> cmd;
    
    private T player;
    
    private IMessage req;
    
    public PlayerCmdInvokeAction(PlayerCmdInvoker<T> invoker, PlayerCommand<T> cmd, T player, IMessage req, ActionQueue queue) {
        super(queue);
        this.invoker = invoker;
        this.cmd = cmd;
        this.player = player;
        this.req = req;
    }

    @Override
    protected void exec() {
        invoker.invoke(cmd, player, req);
    }

    @Override
    public String toString() {
        return "cmd=" + cmd.getClazz();
    }
    
    protected Class<?> getClazz() {
    	return cmd.getClazz();
    }

}

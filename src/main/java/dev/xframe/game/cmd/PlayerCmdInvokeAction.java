package dev.xframe.game.cmd;

import dev.xframe.action.Action;
import dev.xframe.action.ActionQueue;
import dev.xframe.game.player.CmdErrorHandler;
import dev.xframe.game.player.Player;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Prototype;
import dev.xframe.net.codec.IMessage;

/**
 * 通过player task queue 执行的 request command
 * @author luzj
 */
@Prototype
final class PlayerCmdInvokeAction<T extends Player> extends Action {
    
    @Inject
    private CmdErrorHandler handler;
    @Inject
    private PlayerCmdInvoker<T> invoker;
    
    private PlayerCommand<T> cmd;
    
    private T player;
    
    private IMessage req;
    
    public PlayerCmdInvokeAction(PlayerCommand<T> cmd, T player, IMessage req, ActionQueue queue) {
        super(queue);
        this.cmd = cmd;
        this.player = player;
        this.req = req;
    }

    @Override
    protected void exec() {
        try {
            invoker.invoke(cmd, player, req);
        } catch (Throwable ex) {
            handler.onError(player.getPlayerId(), cmd.getClazz(), req, ex);
        }
    }

    @Override
    public String toString() {
        return "cmd=" + cmd.getClazz();
    }
    
    protected Class<?> getClazz() {
    	return cmd.getClazz();
    }

}

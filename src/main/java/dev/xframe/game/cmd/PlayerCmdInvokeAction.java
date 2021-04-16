package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;
import dev.xframe.task.Task;
import dev.xframe.task.TaskLoop;

/**
 * 通过player action loop 执行的 request command
 * @author luzj
 */
final class PlayerCmdInvokeAction<T extends Player> extends Task {
    
    private PlayerCmdInvoker<T> invoker;
    
    private PlayerCommand<T> cmd;
    
    private T player;
    
    private IMessage req;
    
    public PlayerCmdInvokeAction(PlayerCmdInvoker<T> invoker, PlayerCommand<T> cmd, T player, IMessage req, TaskLoop loop) {
        super(loop);
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

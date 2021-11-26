package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.task.Task;
import dev.xframe.task.TaskLoop;

/**
 * 通过player TaskLoop 执行的 request command
 * @author luzj
 */
final class PlayerCmdInvokeTask<T extends Player, M> extends Task {
    
    private PlayerCmdInvoker<T, M> invoker;
    
    private PlayerCmd<T, M> cmd;
    
    private T player;
    
    private M msg;
    
    public PlayerCmdInvokeTask(PlayerCmdInvoker<T, M> invoker, PlayerCmd<T, M> cmd, T player, M msg, TaskLoop loop) {
        super(loop);
        this.invoker = invoker;
        this.cmd = cmd;
        this.player = player;
        this.msg = msg;
    }

    @Override
    protected void exec() {
        invoker.invoke(cmd, player, msg);
    }

    @Override
    public String toString() {
        return "cmd=" + cmd.getClazz();
    }
    
    protected Class<?> getClazz() {
    	return cmd.getClazz();
    }

}

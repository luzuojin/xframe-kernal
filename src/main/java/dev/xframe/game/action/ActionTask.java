package dev.xframe.game.action;

import dev.xframe.game.player.Player;
import dev.xframe.task.Task;
import dev.xframe.utils.XCaught;

public class ActionTask<T extends Player, M> extends Task {
    
    final Action<T, M> act;
    final T  plr;
    final M msg;
    
    ActionTask(Action<T, M> act, T plr, M msg) {
        super(plr.loop());
        this.act = act;
        this.plr = plr;
        this.msg = msg;
    }

    @Override
    protected void exec() {
        exec(act, plr, msg);
    }

    @Override
    protected Class<?> getClazz() {
        return act.getClass();
    }

    public static <T extends Player, M> ActionTask<T, M> of(Action<T, M> act, T plr, M msg) {
        return new ActionTask<>(act, plr, msg);
    }

    public static <T extends Player, M> void exec(Action<T, M> act, T plr, M msg) {
        try {
            act.exec(plr, msg);
        } catch (Exception e) {
            XCaught.throwException(e);
        }
    }

}

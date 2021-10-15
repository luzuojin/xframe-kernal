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
    
    static class Trusted<T extends Player, M> extends ActionTask<T, M> {
        Trusted(Action<T, M> act, T plr, M msg) {
            super(act, plr, msg);
        }
        @Override
        protected void exec() {
            exec0(act, plr, msg);
        }
    }
    
    public static <T extends Player, M> ActionTask<T, M> trusted(Action<T, M> act, T plr, M msg) {
        //trusted action without completion check
        return new Trusted<T, M>(act, plr, msg);
    }
    
    public static <T extends Player, M> ActionTask<T, M> of(Action<T, M> act, T plr, M msg) {
        return new ActionTask<>(act, plr, msg);
    }
    
    public static <T extends Player, M> void exec(Action<T, M> act, T plr, M msg) {
        //ensure action is injected
        ActionBuilder.ensureCompleted(act, plr);
        //do exec
        exec0(act, plr, msg);
    }

    private static <T extends Player, M> void exec0(Action<T, M> act, T plr, M msg) {
        try {
            act.exec(plr, msg);
        } catch (Exception e) {
            XCaught.throwException(e);
        }
    }

}

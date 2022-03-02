package dev.xframe.game.action;

import dev.xframe.task.Task;
import dev.xframe.utils.XCaught;

public class ActionTask<T extends Actor, M> extends Task {
    
    final Action<T, M> action;
    final T actor;
    final M msg;
    
    ActionTask(Action<T, M> action, T actor, M msg) {
        super(actor.loop());
        this.action = action;
        this.actor = actor;
        this.msg = msg;
    }

    @Override
    protected void exec() {
        exec(action, actor, msg);
    }

    @Override
    protected Class<?> getClazz() {
        return action.getClass();
    }

    public static <T extends Actor, M> ActionTask<T, M> of(Action<T, M> action, T actor, M msg) {
        return new ActionTask<>(action, actor, msg);
    }

    public static <T extends Actor, M> void exec(Action<T, M> action, T actor, M msg) {
        try {
            action.exec(actor, msg);
        } catch (Exception e) {
            XCaught.throwException(e);
        }
    }

}

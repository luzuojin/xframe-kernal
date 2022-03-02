package dev.xframe.game.action;

import dev.xframe.task.TaskLoop;

public class Actor {

    private final long id;
    private final TaskLoop loop;

    public Actor(long id, TaskLoop loop) {
        this.loop = loop;
        this.id = id;
    }

    public final long id() {
        return this.id;
    }
    public final TaskLoop loop() {
        return this.loop;
    }

    public final <M> void accept(M msg) {
        exec(Actions.makeByMsg(this, msg), msg);
    }
    public final <T extends Actor> void accept(Runnable<T> r) {
        exec(Actions.makeByRunnable(this, r), r);
    }
    @SuppressWarnings("unchecked")
    private <T extends Actor, M> void exec(Action<T, M> action, M msg) {
        final T actor = (T) this;
        if(loop.inLoop()) {
            ActionTask.exec(action, actor, msg);
        } else {
            ActionTask.of(action, actor, msg).checkin();
        }
    }

}

package dev.xframe.game.action;

/**
 * 接受一个runnable为msg
 * @param <T>
 */
class RunnableAction<T extends Actor> implements Action<T, Runnable<T>> {
    @Override
    public void exec(T actor, Runnable<T> msg) throws Exception {
        msg.run(actor);
    }
}

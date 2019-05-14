package dev.xframe.action;

public final class RunnableAction extends Action {

    private final Runnable runnable;
    
    public RunnableAction(ActionQueue queue, Runnable runnable) {
        super(queue);
        this.runnable = runnable;
    }

    @Override
    protected final void exec() {
        runnable.run();
    }

    public static final RunnableAction of(ActionQueue queue, Runnable runnable) {
        return new RunnableAction(queue, runnable);
    }
    
}

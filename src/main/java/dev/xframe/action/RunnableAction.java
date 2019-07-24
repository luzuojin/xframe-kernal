package dev.xframe.action;

public final class RunnableAction extends Action {

    private final Runnable runnable;
    
    public RunnableAction(ActionLoop loop, Runnable runnable) {
        super(loop);
        this.runnable = runnable;
    }

    @Override
    protected final void exec() {
        runnable.run();
    }

    public static final RunnableAction of(ActionLoop loop, Runnable runnable) {
        return new RunnableAction(loop, runnable);
    }
    
}

package dev.xframe.task;

public final class RunnableTask extends Task {

    private final Runnable runnable;
    
    public RunnableTask(TaskLoop loop, Runnable runnable) {
        super(loop);
        this.runnable = runnable;
    }

    @Override
    protected final void exec() {
        runnable.run();
    }

    @Override
	protected Class<?> getClazz() {
		return runnable.getClass();
	}

	public static final RunnableTask of(TaskLoop loop, Runnable runnable) {
        return new RunnableTask(loop, runnable);
    }
    
}

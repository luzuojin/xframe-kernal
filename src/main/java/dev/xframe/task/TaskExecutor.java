package dev.xframe.task;

/**
 * 
 * @author luzj
 *
 */
public interface TaskExecutor {
    
    default TaskLoop newLoop() {
        return new TaskLoop.Queued(this);
    }
    default TaskExecutor bind() {
        return this;
    }
    
    public void schedule(DelayTask task);

    public void execute(Runnable task);
    
    public void shutdown();
    
}

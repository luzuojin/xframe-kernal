package dev.xframe.task;

import java.util.concurrent.Executor;

/**
 * 
 * @author luzj
 *
 */
public interface TaskExecutor extends Executor {
    
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

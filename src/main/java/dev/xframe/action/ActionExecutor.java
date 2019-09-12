package dev.xframe.action;

/**
 * 
 * @author luzj
 *
 */
public interface ActionExecutor {
    
    default ActionLoop newLoop() {
        return new ActionLoop(this);
    }
    
    public void schedule(DelayAction action);

    public void execute(Runnable action);
    
    public void shutdown();
    
    public void setThreadsCount(int nThreads);
    
}

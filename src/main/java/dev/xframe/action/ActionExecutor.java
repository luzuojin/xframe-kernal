package dev.xframe.action;

/**
 * 
 * @author luzj
 *
 */
public interface ActionExecutor {
    
    public ActionLoop defaultLoop();
    
    public void schedule(DelayAction action);

    public void execute(Runnable action);
    
    public void shutdown();
    
    public void setThreadsCount(int nThreads);
    
}

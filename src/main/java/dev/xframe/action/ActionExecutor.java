package dev.xframe.action;

/**
 * 
 * @author luzj
 *
 */
public interface ActionExecutor {
    
    public ActionQueue defaultQueue();
    
    public void delayCheck(DelayAction action);

    public void execute(Runnable action);
    
    public void shutdown();
    
    public void setThreadsCount(int nThreads);
    
}

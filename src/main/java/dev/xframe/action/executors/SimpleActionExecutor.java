package dev.xframe.action.executors;

import java.util.concurrent.ExecutorService;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.DelayAction;
import dev.xframe.action.scheduled.ScheduledExecutor;

public class SimpleActionExecutor implements ActionExecutor {
    
    private final ExecutorService executor;
    
    private volatile boolean isRunning = true;
    //delay set
    private volatile ScheduledExecutor scheduler;
    
    /**
     * 执行action队列的线程池
     * @param corePoolSize 最小线程数
     * @param maxPoolSize 最大线程数
     * @param name 线程名
     */
    public SimpleActionExecutor(ExecutorService executor) {
        this(executor, new ScheduledExecutor());
    }
    
    public SimpleActionExecutor(ExecutorService executor, ScheduledExecutor scheduler) {
        this.executor = executor;
        this.scheduler = scheduler;
    }
    
    public void schedule(DelayAction action) {
        this.scheduler.checkin(action);
    }
    
    public void execute(Runnable action) {
        executor.execute(action);
    }
    
    public synchronized void shutdown() {
        if(isRunning) {
            if (!executor.isShutdown())
                executor.shutdown();
            
            if(scheduler != null)
                scheduler.shutdown();
            
            isRunning = false;
        }
    }

}

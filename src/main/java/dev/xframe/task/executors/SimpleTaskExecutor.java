package dev.xframe.task.executors;

import java.util.concurrent.ExecutorService;

import dev.xframe.task.DelayTask;
import dev.xframe.task.TaskExecutor;
import dev.xframe.task.scheduled.ScheduledTimer;

public class SimpleTaskExecutor implements TaskExecutor {
    
    private final ExecutorService executor;
    
    private final ScheduledTimer scheduler;
    
    private volatile boolean isRunning = true;
    
    /**
     * 执行task队列的线程池
     * @param corePoolSize 最小线程数
     * @param maxPoolSize 最大线程数
     * @param name 线程名
     */
    public SimpleTaskExecutor(ExecutorService executor) {
        this(executor, new ScheduledTimer());
    }
    
    public SimpleTaskExecutor(ExecutorService executor, ScheduledTimer scheduler) {
        this.executor = executor;
        this.scheduler = scheduler;
    }
    
    public void schedule(DelayTask task) {
        this.scheduler.checkin(task);
    }
    
    public void execute(Runnable task) {
        executor.execute(task);
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

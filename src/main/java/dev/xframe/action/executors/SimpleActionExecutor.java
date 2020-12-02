package dev.xframe.action.executors;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.DelayAction;

public class SimpleActionExecutor implements ActionExecutor {
    
    private final String name;
    
    private final ExecutorService executor;
    
    private final Function<String, DelayScheduler> schedulerFactory;
    
    private volatile boolean isRunning = true;
    //delay set
    private volatile DelayScheduler scheduler;
    
    /**
     * 执行action队列的线程池
     * @param corePoolSize 最小线程数
     * @param maxPoolSize 最大线程数
     * @param name 线程名
     */
    public SimpleActionExecutor(String name, ExecutorService executor) {
        this(name, executor, DelaySchedulers::make);
    }
    
    public SimpleActionExecutor(String name, ExecutorService executor, Function<String, DelayScheduler> schedulerFactory) {
        this.name = name;
        this.schedulerFactory = schedulerFactory;
        this.executor = executor;
    }
    
    public void schedule(DelayAction action) {
        if(this.scheduler == null) {
            setupScheduler();
        }
        this.scheduler.checkin(action);
    }
    
    private synchronized void setupScheduler() {
        if(this.scheduler == null) {
            this.scheduler = schedulerFactory.apply(name);
        }
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

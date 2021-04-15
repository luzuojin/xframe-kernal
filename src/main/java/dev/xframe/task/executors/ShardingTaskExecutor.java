package dev.xframe.task.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import dev.xframe.task.DelayTask;
import dev.xframe.task.TaskExecutor;
import dev.xframe.task.scheduled.ScheduledTimer;
import dev.xframe.utils.XThreadFactory;

public class ShardingTaskExecutor implements TaskExecutor {
    
    private final String name;
    
    private final IntUnaryOperator chooser;
    
    private final AtomicInteger sIndex = new AtomicInteger();
    
    private final TaskExecutor[] internals;
    
    private final ScheduledTimer scheduler;
    
    public ShardingTaskExecutor(int nThreads, String name) {
        this.name = name;
        this.chooser = newChooser(nThreads);
        this.internals = new TaskExecutor[nThreads];
        this.scheduler = new ScheduledTimer();
        
        XThreadFactory factory = new XThreadFactory(this.name);
        for (int i = 0; i < nThreads; i++) {
            internals[i] = new SimpleTaskExecutor(newExecutorService(factory), scheduler);
        }
    }

    private ExecutorService newExecutorService(XThreadFactory factory) {
        return new SingleThreadExecutor(factory, new LinkedTransferQueue<Runnable>());
    }
    
    public void schedule(DelayTask task) {
        this.scheduler.checkin(task);
    }
    
    private IntUnaryOperator newChooser(final int nThreads) {
        if((nThreads & -nThreads) == nThreads) {//pow of 2
            final int basis = nThreads - 1;
            return (seed) -> (seed & basis);
        }
        return (seed) -> (seed % nThreads);
    }
    
    private TaskExecutor choose(int seed) {
        return internals[chooser.applyAsInt(seed)];
    }
    
    public TaskExecutor bind() {
        return choose(sIndex.getAndIncrement());
    }

    public void execute(Runnable task) {
        choose(task.hashCode()).execute(task);
    }
    
    public void shutdown() {
        for (TaskExecutor e : internals) {
            if(e != null) e.shutdown();
        }
    }
}
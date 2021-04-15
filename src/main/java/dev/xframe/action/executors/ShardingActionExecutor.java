package dev.xframe.action.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.DelayAction;
import dev.xframe.action.scheduled.ScheduledExecutor;
import dev.xframe.utils.XThreadFactory;

public class ShardingActionExecutor implements ActionExecutor {
    
    private final String name;
    
    private final IntUnaryOperator chooser;
    
    private final AtomicInteger sIndex = new AtomicInteger();
    
    private final ActionExecutor[] internals;
    
    private final ScheduledExecutor scheduler;
    
    public ShardingActionExecutor(int nThreads, String name) {
        this.name = name;
        this.chooser = newChooser(nThreads);
        this.internals = new ActionExecutor[nThreads];
        this.scheduler = new ScheduledExecutor();
        
        XThreadFactory factory = new XThreadFactory(this.name);
        for (int i = 0; i < nThreads; i++) {
            internals[i] = new SimpleActionExecutor(newExecutorService(factory), scheduler);
        }
    }

    private ExecutorService newExecutorService(XThreadFactory factory) {
        return new SingleThreadExecutor(factory, new LinkedTransferQueue<Runnable>());
    }
    
    public void schedule(DelayAction action) {
        this.scheduler.checkin(action);
    }
    
    private IntUnaryOperator newChooser(final int nThreads) {
        if((nThreads & -nThreads) == nThreads) {//pow of 2
            final int basis = nThreads - 1;
            return (seed) -> (seed & basis);
        }
        return (seed) -> (seed % nThreads);
    }
    
    private ActionExecutor choose(int seed) {
        return internals[chooser.applyAsInt(seed)];
    }
    
    public ActionExecutor bind() {
        return choose(sIndex.getAndIncrement());
    }

    public void execute(Runnable action) {
        choose(action.hashCode()).execute(action);
    }
    
    public void shutdown() {
        for (ActionExecutor e : internals) {
            if(e != null) e.shutdown();
        }
    }
}
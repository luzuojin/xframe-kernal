package dev.xframe.action.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.DelayAction;
import dev.xframe.utils.XThreadFactory;

public class ShardingActionExecutor implements ActionExecutor {
    
    private final String name;
    
    private final int nThreads;
    
    private final AtomicInteger sIndex = new AtomicInteger();
    
    private final ActionExecutor[] internals;
    
    private volatile DelayScheduler scheduler;
    
    public ShardingActionExecutor(int nThreads, String name) {
        this.name = name;
        this.nThreads = nThreads;
        this.internals = new ActionExecutor[nThreads];
        
        XThreadFactory factory = new XThreadFactory(this.name);
        Function<String, DelayScheduler> schedulerFactory = this::setupScheduler;
        for (int i = 0; i < internals.length; i++) {
            internals[i] = new SimpleActionExecutor(this.name, newExecutorService(factory), schedulerFactory);
        }
    }

    private ExecutorService newExecutorService(XThreadFactory factory) {
        return new SingleThreadExecutor(factory, new LinkedTransferQueue<Runnable>());
    }
    
    public void schedule(DelayAction action) {
        if(this.scheduler == null) {
            setupScheduler(this.name);
        }
        this.scheduler.checkin(action);
    }
    
    private synchronized DelayScheduler setupScheduler(String name) {
        if(this.scheduler == null) {
            this.scheduler = DelaySchedulers.make(name);
        }
        return this.scheduler;
    }
    
    private ActionExecutor choose(int seed) {
        int nts = this.nThreads;
        if((nts & -nts) == nts) {//pow of 2
            final int basis = nts - 1;
            return internals[seed & basis];
        }
        return internals[seed % nThreads];
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
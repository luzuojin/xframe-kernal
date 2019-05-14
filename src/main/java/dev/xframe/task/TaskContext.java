package dev.xframe.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import dev.xframe.injection.Configurator;
import dev.xframe.injection.Eventual;
import dev.xframe.tools.ThreadsFactory;

@Configurator
public class TaskContext implements Eventual {
    
    private static final int CTL_NONE = 0;  //ctl none
    private static final int CTL_SETUP = 1;  //task context setup
    private static final int CTL_LAUNCH = 2; //application launch
    
    private AtomicInteger ctl = new AtomicInteger(CTL_NONE);
    
    private Map<Object, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();;
    
    private ScheduledThreadPoolExecutor executor;

    private boolean ctlPlus(int ctl) {
        return ctlNil(this.ctl.getAndAccumulate(ctl, (now, c) -> now | c), ctl);
    }
    
    private boolean ctlNil(int ctl) {
        return ctlNil(this.ctl.get(), ctl);
    }
    
    private boolean ctlNil(int now, int ctl) {
        return (now & ctl) == 0;
    }
    
    private void defaultSetup() {
        this.setup(Math.min(1, Runtime.getRuntime().availableProcessors() / 2));
    }
    
    public void setup(int nthreads) {
        if(ctlPlus(CTL_SETUP)) {
            executor = new ScheduledThreadPoolExecutor(nthreads, new ThreadsFactory("Tasks"));
            regist(Task.period("Clear task futures", 10, this::clearTaskFutures));
        } else if(executor.getCorePoolSize() < nthreads){
            executor.setCorePoolSize(nthreads);
        }
    }

    public void regist(Task task) {
        futures.put(task.key(), schedule(task));
    }
    
    public boolean unregist(Object key) {
        if(key != null) {
            ScheduledFuture<?> f = futures.get(key);
            if(f != null) {
                f.cancel(false);
                return true;
            }
        }
        return false;
    }
    
    public boolean unregist(Predicate<Object> filter) {
        return unregist(futures.keySet().stream().filter(filter).findAny().orElse(null));
    }
    
    private ScheduledFuture<?> schedule(Task task) {
        if(ctlNil(CTL_LAUNCH)) {
            return new TaskRegistator(task);
        } else {
            if(ctlNil(CTL_SETUP)) {
                this.defaultSetup();
            }
            if(task.period == -1) {//once task
                return executor.schedule(task, task.delay, task.unit);
            } else {
                return executor.scheduleWithFixedDelay(task, task.delay, task.period, task.unit);
            }
        }
    }

    @Override
    public void eventuate() {
        if(ctlNil(CTL_SETUP) && !futures.isEmpty()) {
            this.defaultSetup();
        }
        if(ctlPlus(CTL_LAUNCH)) {
            new ArrayList<>(futures.values()).stream()
                .filter (f -> f instanceof TaskRegistator && !f.isCancelled() && !f.isDone())
                .forEach(t -> ((TaskRegistator) t).registTo(TaskContext.this));
        }
    }
    
    public void clearTaskFutures() {
        Iterator<Entry<Object, ScheduledFuture<?>>> it = futures.entrySet().iterator();
        while(it.hasNext()) {
            Entry<Object, ScheduledFuture<?>> next = it.next();
            ScheduledFuture<?> nextVal = next.getValue();
            if(nextVal == null || nextVal.isCancelled() || nextVal.isDone()) {
                futures.remove(next.getKey());
            }
        }
    }
    
    public void shutdown() {
        executor.shutdownNow();
    }

}

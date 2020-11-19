package dev.xframe.action;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.xframe.utils.XThreadLocal;

public class ActionLoop {
    
    private ActionExecutor executor;
    private ConcurrentLinkedQueue<Runnable> queue;
    private AtomicBoolean isRunning;
    
    public ActionLoop(ActionExecutor executor) {
        this.executor = executor.binding();
        this.queue = new ConcurrentLinkedQueue<>();
        this.isRunning = new AtomicBoolean(false);
    }
    
    void schedule(DelayAction action) {
        executor.schedule(action);
    }
    
    void checkin(Runnable action) {
        this.queue.offer(action);
        
        if(this.isRunning.compareAndSet(false, true)){
           this.execNext();
        }
    }

    private Runnable execNext() {
        Runnable next = this.queue.peek();
        if(next != null) {
            executor.execute(next);
        } else {
            this.isRunning.set(false);
            
            //double check
            next = this.queue.peek();
            if(next != null && this.isRunning.compareAndSet(false, true)) {
                executor.execute(next);
            }
        }
        return next;
    }
    
    void checkout(Runnable action) {
        Runnable poll = this.queue.poll();
        if(poll != action) {
        	Action.logger.warn("Action can`t call run() directly");
        }
        this.execNext();
    }
    
    int size() {
        return queue.size();
    }
    
    public boolean inLoop() {
        return this == getCurrent();
    }
    
    static XThreadLocal<ActionLoop> tloop = new XThreadLocal<>();
    static ActionLoop getCurrent() {
        return tloop.get();
    }
    static void setCurrent(ActionLoop loop) {
        tloop.set(loop);
    }
    static void unsetCurrent() {
        tloop.remove();
    }
    
}

package dev.xframe.action;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActionLoop {
    
    private ActionExecutor executor;
    private ConcurrentLinkedQueue<Runnable> queue;
    private AtomicBoolean isRunning;
    
    public ActionLoop(ActionExecutor executor) {
        this.executor = executor;
        this.queue = new ConcurrentLinkedQueue<>();
        this.isRunning = new AtomicBoolean(false);
    }
    
    void schedule(DelayAction action) {
        executor.schedule(action);
    }
    
    void checkin(Action action) {
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
    
    static ActionLoop getCurrent() {
    	Thread thread = Thread.currentThread();
		return (thread instanceof ActionThread) ? ((ActionThread) thread).getAttach() : null;
    }
    
    static void setCurrent(ActionLoop loop) {
    	Thread thread = Thread.currentThread();
    	if(thread instanceof ActionThread) {
    		((ActionThread) thread).setAttach(loop);
    	}
    }
    
    static void unsetCurrent() {
    	Thread thread = Thread.currentThread();
    	if(thread instanceof ActionThread) {
    		((ActionThread) thread).unsetAttach();
    	}
    }
    
}
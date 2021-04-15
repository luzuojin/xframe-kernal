package dev.xframe.action;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.xframe.utils.XThreadLocal;

public abstract class ActionLoop {
    
    protected ActionExecutor executor;
    public ActionLoop(ActionExecutor executor) {
        this.executor = executor.bind();
    }
    
    abstract void schedule(DelayAction action);
    
    abstract void checkin(Runnable action);
    
    abstract void checkout(Runnable action);
    
    public static class Queued extends ActionLoop {
    	private ConcurrentLinkedQueue<Runnable> queue;
    	private AtomicBoolean isRunning;
    	
    	public Queued(ActionExecutor executor) {
    		super(executor);
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
    }
    
    public static class Direct extends ActionLoop {
    	
		public Direct(ActionExecutor executor) {
			super(executor);
		}
		
		void schedule(DelayAction action) {
			executor.schedule(action);
		}
		
		void checkin(Runnable action) {
			executor.execute(action);
		}
		
		void checkout(Runnable action) {
			//do nothing
		}
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

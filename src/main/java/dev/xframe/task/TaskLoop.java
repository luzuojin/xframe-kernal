package dev.xframe.task;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.xframe.utils.XThreadLocal;

public abstract class TaskLoop {
    
    protected TaskExecutor executor;
    
    public TaskLoop(TaskExecutor executor) {
        this.executor = executor.bind();
    }
    
    void schedule(DelayTask task) {
    	executor.schedule(task);
    }
    
    abstract void checkin(Runnable task);
    
    abstract void checkout(Runnable task);
    
    public static class Queued extends TaskLoop {
    	private ConcurrentLinkedQueue<Runnable> queue;
    	private AtomicBoolean isRunning;
    	
    	public Queued(TaskExecutor executor) {
    		super(executor);
            this.queue = new ConcurrentLinkedQueue<>();
            this.isRunning = new AtomicBoolean(false);
        }
        
        void checkin(Runnable task) {
            this.queue.offer(task);
            
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
        
        void checkout(Runnable task) {
            Runnable poll = this.queue.poll();
            if(poll != task) {
            	Task.logger.warn("Task can`t call run() directly");
            }
            this.execNext();
        }
    }
    
    public static class Direct extends TaskLoop {
    	
		public Direct(TaskExecutor executor) {
			super(executor);
		}
		
		void checkin(Runnable task) {
			executor.execute(task);
		}
		
		void checkout(Runnable task) {
			//do nothing
		}
    }
    
    public boolean inLoop() {
        return this == getCurrent();
    }
    
    static XThreadLocal<TaskLoop> tloop = new XThreadLocal<>();
    static TaskLoop getCurrent() {
        return tloop.get();
    }
    static void setCurrent(TaskLoop loop) {
        tloop.set(loop);
    }
    static void unsetCurrent() {
        tloop.remove();
    }
    
}

package dev.xframe.action;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.tools.ThreadsFactory;

public class ActionExecutors {
    
    private final static List<ActionExecutor> executors = new LinkedList<ActionExecutor>();
    
    public static ActionExecutor newSingle(String name) {
        return new ThreadPoolActionExecutor(1, name);
    }
    
    public static ActionExecutor newFixed(String name, int nThreads) {
        return new ThreadPoolActionExecutor(nThreads, name);
    }
    
    public static void shutdown() {
        for (ActionExecutor executor : executors) {
            executor.shutdown();
        }
    }
    
    public static List<ActionExecutor> getExecutors() {
    	return executors;
    }
    
    public static class ThreadPoolActionExecutor implements ActionExecutor {
        
        private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
        
        private final String name;
        private final ThreadPoolExecutor executor;
        
        private boolean isRunning = true;
        
        //delay set
        private ActionLoop defaultLoop;
        private ScheduleThread scheduleThread;
        
        /**
         * 执行action队列的线程池
         * @param corePoolSize 最小线程数
         * @param maxPoolSize 最大线程数
         * @param name 线程名
         */
        private ThreadPoolActionExecutor(final int poolSize, final String name) {
            this.name = name == null ? "customer" : name;
            
            executor = new ThreadPoolExecutor(
                            poolSize, Integer.MAX_VALUE,  //pool size
                            5, TimeUnit.MINUTES, //alive time
                            new LinkedTransferQueue<>(),
                            new ThreadsFactory(this.name){
								protected Thread newThread0(Runnable target) {
									return new ActionThread(group, target, getThreadName());
								}
                            },
                            new DiscardPolicy());
            
            executors.add(this);
        }
        
		public ActionLoop defaultLoop() {
            if(this.defaultLoop == null) {
                setupDefaultLoop();
            }
            return this.defaultLoop;
        }
        
        synchronized void setupDefaultLoop() {
            if(this.defaultLoop == null) {
                this.defaultLoop = new ActionLoop(this);
            }
        }

        public void schedule(DelayAction action) {
            if(this.scheduleThread == null) {
                setupScheduleThread();
            }
            
            this.scheduleThread.checkin(action);
        }
        
        private synchronized void setupScheduleThread() {
            if(this.scheduleThread == null) {
                this.scheduleThread = new ScheduleThread(name);
                this.scheduleThread.start();
            }
        }

        public void execute(Runnable action) {
            executor.execute(action);
        }
        
        public synchronized void shutdown() {
            if(isRunning) {
                if (!executor.isShutdown()) {
                    executor.shutdown();
                }
                
                if(scheduleThread != null)
                    scheduleThread.shutdown();
                
                isRunning = false;
            }
        }
        
        static class ScheduleThread extends Thread {

            private DelayQueue<DelayAction> queue;
            private boolean isRunning;
            private int counter;

            public ScheduleThread(String prefix) {
                super(prefix + "-thread-dc");
                setPriority(Thread.MAX_PRIORITY); // 给予高优先级
                queue = new DelayQueue<>();
                isRunning = true;
            }

            public boolean isRunning() {
                return isRunning;
            }

            public void shutdown() {
                if (isRunning) isRunning = false;
            }

            @Override
            public void run() {
                while (isRunning) {
                    try {
                        DelayAction action = queue.take();
                        long now = System.currentTimeMillis();
                        if (!action.tryExec(now)) {
                            checkin(action);
                        }
                        if (++counter > 1024) {
                            int size = queue.size();
                            if (size > 32)
                                logger.info("Waiting delay actions [{}]", size);
                            
                            counter = 0;
                        }
                    } catch (Throwable e) {
                        logger.error(getName() + " Error. ", e);
                    }
                }
            }

            public void checkin(DelayAction delayAction) {
                queue.offer(delayAction);
            }
        }
        
        public String getName() {
            return this.name;
        }
        public int getWaitingActionsCount() {
        	return executor.getQueue().size();
        }
        public long getCompletedActionsCount() {
        	return executor.getCompletedTaskCount();
        }
		public int getActiveThreadsCount() {
			return executor.getActiveCount();
		}
		public int getPooledThreadsCount() {
			return executor.getPoolSize();
		}
		public void setThreadsCount(int nThreads) {
            executor.setCorePoolSize(nThreads);
        }
		public int getThreadsCount() {
			return executor.getCorePoolSize();
		}
		public ThreadFactory getThreadFactory() {
		    return executor.getThreadFactory();
		}
    }

}

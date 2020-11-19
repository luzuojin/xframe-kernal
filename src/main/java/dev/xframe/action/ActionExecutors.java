package dev.xframe.action;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.utils.XProperties;
import dev.xframe.utils.XThreadFactory;
import io.netty.util.HashedWheelTimer;

public class ActionExecutors {
    
    private final static List<ActionExecutor> executors = new LinkedList<ActionExecutor>();
    
    public static ActionExecutor newSingle(String name) {
        return hold(new ThreadPoolActionExecutor(1, name));
    }
    
    public static ActionExecutor newFixed(String name, int nThreads) {
        return hold(new ThreadPoolActionExecutor(nThreads, name));
    }
    
    public static ActionExecutor newBindable(String name, int nThreads) {
        return hold(new BindableActionExecutor(nThreads, name));
    }
    
    private static ActionExecutor hold(ActionExecutor executor) {
        executors.add(executor);
        return executor;
    }
    
    public static void shutdown() {
        for (ActionExecutor executor : executors) {
            executor.shutdown();
        }
    }
    
    public static List<ActionExecutor> getExecutors() {
    	return executors;
    }
    
    public static class BindableActionExecutor implements ActionExecutor {
        
        private final String name;
        
        private final int nThreads;
        
        private final AtomicInteger sIndex = new AtomicInteger();
        
        private final ActionExecutor[] internals;
        
        private DelayScheduler scheduler;
        
        public BindableActionExecutor(int nThreads, String name) {
            this.name = name;
            this.nThreads = nThreads;
            this.internals = new ActionExecutor[nThreads];
            for (int i = 0; i < internals.length; i++) {
                XThreadFactory factory = new XThreadFactory(this.name);
                internals[i] = new ThreadPoolActionExecutor(1, this.name, factory, this::setupScheduler);
            }
        }
        
        public void schedule(DelayAction action) {
            if(this.scheduler == null) {
                setupScheduler(this.name);
            }
            this.scheduler.checkin(action);
        }
        
        private synchronized DelayScheduler setupScheduler(String name) {
            if(this.scheduler == null) {
                this.scheduler = DelaySchedulerFactory.make(name);
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
        
        public ActionExecutor binding() {
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
    
    public static class ThreadPoolActionExecutor implements ActionExecutor {
        
        private final String name;
        
        private final ThreadPoolExecutor executor;
        
        private final Function<String, DelayScheduler> schedulerFactory;
        
        private volatile boolean isRunning = true;
        //delay set
        private DelayScheduler scheduler;
        
        /**
         * 执行action队列的线程池
         * @param corePoolSize 最小线程数
         * @param maxPoolSize 最大线程数
         * @param name 线程名
         */
        private ThreadPoolActionExecutor(final int poolSize, final String name) {
            this(poolSize, name, new XThreadFactory(name), DelaySchedulerFactory::make);
        }
        
        private ThreadPoolActionExecutor(final int nThreads, final String name, ThreadFactory threadFactory, Function<String, DelayScheduler> schedulerFactory) {
            this.name = name;
            this.schedulerFactory = schedulerFactory;
            
            executor = new ThreadPoolExecutor(
                            nThreads, nThreads,  //pool size
                            5, TimeUnit.MINUTES, //alive time
                            new LinkedTransferQueue<>(),
                            threadFactory,
                            new DiscardPolicy());
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
                if (!executor.isShutdown()) {
                    executor.shutdown();
                }
                
                if(scheduler != null)
                    scheduler.shutdown();
                
                isRunning = false;
            }
        }
    }
    
    static class DelaySchedulerFactory {
        static DelayScheduler hashedwheel;//hashedwheel全局只用一个实例
        static boolean useHashedWheel = XProperties.getAsBool("xframe.hashedwheel", true);
        static int tickDuration = XProperties.getAsInt("xframe.hashedwheel.tickduration", 100);
        static synchronized DelayScheduler make(String name) {
            if(useHashedWheel) {
                if(hashedwheel == null) {
                    hashedwheel = new HWDelayScheduler(tickDuration);
                    hashedwheel.startup();
                }
                return hashedwheel;
            } else {
                DelayScheduler s = new TDelayScheduler(name);
                s.startup();
                return s;
            }
        }
    }
    
    static interface DelayScheduler {
        void startup();
        void shutdown();
        void checkin(DelayAction action);
    }
    
    static class TDelayScheduler extends Thread implements DelayScheduler {
        private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
        private DelayQueue<DelayAction> queue;
        private volatile boolean isRunning;
        private int checkedCount;

        public TDelayScheduler(String prefix) {
            super(prefix + "-scheduler");
            setPriority(Thread.MAX_PRIORITY); // 给予高优先级
            queue = new DelayQueue<>();
            isRunning = true;
        }
        public void shutdown() {
            if (isRunning)
                isRunning = false;
        }
        @Override
        public void startup() {
            start();
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
                    if (++checkedCount > 1024) {
                        int size = queue.size();
                        if (size > 32)
                            logger.info("Waiting delay actions [{}]", size);
                        
                        checkedCount = 0;
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
    
    static class HWDelayScheduler implements DelayScheduler {
        final static TimeUnit unit = TimeUnit.MILLISECONDS;
        final HashedWheelTimer timer;
        public HWDelayScheduler(int tickDuration) {
            timer = new HashedWheelTimer(new XThreadFactory("hwdelays"), tickDuration, unit);
        }
        @Override
        public void startup() {
            timer.start();
        }
        public void shutdown() {
            timer.stop();
        }
        public void checkin(DelayAction action) {
            timer.newTimeout(t->runDelay(action), action.getDelay(unit), unit);
        }
        private void runDelay(DelayAction action) {
            if(!action.tryExec(System.currentTimeMillis())) {
                checkin(action);
            }
        }
    }

}

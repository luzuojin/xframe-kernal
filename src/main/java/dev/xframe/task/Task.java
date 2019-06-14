package dev.xframe.task;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Task implements Runnable {
    
    protected static Logger logger = LoggerFactory.getLogger(Task.class);
    
    public final String name;
    public final int delay;
    public final int period;
    public final TimeUnit unit;
    
    public Task(String name, int delay, int period, TimeUnit unit) {
        this.name = name;
        this.delay = delay;
        this.period = period;
        this.unit = unit;
    }
    
    public Object key() {
        return this;
    }
    
    public final void run() {
        try {
            this.exec();
        } catch (Throwable ex) {
            logger.error("Run task [" + name + "] error", ex);
        }
    }

    public abstract void exec();
    
    /**
     * delay by minutes
     */
    public static Task once(String name, int delay, Runnable runnable) {
        return new RunnaleTask(name, delay, -1, TimeUnit.MINUTES, runnable);
    }
    
    public static Task once(String name, int delay, TimeUnit unit, Runnable runnable) {
        return new RunnaleTask(name, delay, -1, unit, runnable);
    }
    
    
    /**
     * delay && period by 1 minutes
     */
    public static Task period(String name, Runnable runnable) {
        return new RunnaleTask(name, 1, 1, TimeUnit.MINUTES, runnable);
    }
    
    /**
     * period by minutes
     */
    public static Task period(String name, int period, Runnable runnable) {
        return new RunnaleTask(name, period, period, TimeUnit.MINUTES, runnable);
    }
    
    public static Task period(String name, int delay, int period, TimeUnit unit, Runnable runnable) {
        return new RunnaleTask(name, delay, period, unit, runnable);
    }
    
    static class RunnaleTask extends Task {
        final Runnable runnable;
        public RunnaleTask(String name, int delay, int period, TimeUnit unit, Runnable runnable) {
            super(name, delay, period, unit);
            this.runnable = runnable;
        }
        @Override
        public void exec() {
            runnable.run();
        }
        @Override
        public Object key() {
            return runnable;
        }
    }

}

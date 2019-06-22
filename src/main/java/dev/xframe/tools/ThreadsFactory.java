package dev.xframe.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @see DefaultThreadFactory 增加可辨识的线程名前缀
 * @author luzj
 */
public  class ThreadsFactory implements ThreadFactory {
    
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected final String namePrefix;

    public ThreadsFactory(String name) {
        group = new ThreadGroup(name);
        namePrefix = name + "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = newThread0(r);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

	protected Thread newThread0(Runnable r) {
		return new Thread(group, r, getThreadName());
	}

	protected String getThreadName() {
		return namePrefix + threadNumber.getAndIncrement();
	}
    
}

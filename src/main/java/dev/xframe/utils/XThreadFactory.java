package dev.xframe.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author luzj
 */
public class XThreadFactory implements ThreadFactory {
    
    protected final ThreadGroup group;
    protected final AtomicInteger nextId;
    protected final String namePrefix;

    public XThreadFactory(String name) {
        nextId = new AtomicInteger(1);
        group = new ThreadGroup(name);
        namePrefix = name;
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
		return new XThread(group, r, getThreadName());
	}

	protected String getThreadName() {
		return namePrefix + "-" + nextId.getAndIncrement();
	}
    
}

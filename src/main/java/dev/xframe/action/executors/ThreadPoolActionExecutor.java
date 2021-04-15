package dev.xframe.action.executors;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

import dev.xframe.action.scheduled.ScheduledExecutor;
import dev.xframe.utils.XThreadFactory;

public class ThreadPoolActionExecutor extends SimpleActionExecutor {
    
    /**
     * 执行action队列的线程池
     * @param nThreads 线程数
     * @param name 线程名
     */
    public ThreadPoolActionExecutor(final int nThreads, final String name) {
        this(nThreads, new XThreadFactory(name), new ScheduledExecutor());
    }
    
    public ThreadPoolActionExecutor(final int nThreads, ThreadFactory threadFactory, ScheduledExecutor scheduler) {
        super(newExecutorService(nThreads, threadFactory), scheduler);
    }

    private static ThreadPoolExecutor newExecutorService(int nThreads, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(
                        nThreads, nThreads,  //pool size
                        5, TimeUnit.MINUTES, //alive time
                        new LinkedTransferQueue<>(),
                        threadFactory,
                        new DiscardPolicy());
    }
    
}
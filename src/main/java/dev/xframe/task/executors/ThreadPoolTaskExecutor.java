package dev.xframe.task.executors;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;

import dev.xframe.task.scheduled.ScheduledTimer;
import dev.xframe.utils.XThreadFactory;

public class ThreadPoolTaskExecutor extends SimpleTaskExecutor {
    
    /**
     * 执行Task队列的线程池
     * @param nThreads 线程数
     * @param name 线程名
     */
    public ThreadPoolTaskExecutor(final int nThreads, final String name) {
        this(nThreads, new XThreadFactory(name), new ScheduledTimer());
    }
    
    public ThreadPoolTaskExecutor(final int nThreads, ThreadFactory threadFactory, ScheduledTimer scheduler) {
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
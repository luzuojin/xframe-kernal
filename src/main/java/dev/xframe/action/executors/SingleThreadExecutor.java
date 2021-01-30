package dev.xframe.action.executors;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 改造自netty.SingleThreadEventExecutor
 * @author luzj
 */
public class SingleThreadExecutor extends AbstractExecutorService {
    
    @FunctionalInterface
    public static interface RejectedHandler {
        public void rejected(Runnable task, SingleThreadExecutor executor);
    }
    
    public static final RejectedHandler DISCARD = (t, e)->{};
    
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadExecutor.class);
    
    private static final long DEF_SHUTDOWN_TIMEOUT_TIME = TimeUnit.SECONDS.toNanos(60);//最多在调用shutdown之后保持继续执行任务1min
    private static final long DEF_SHUTDOWN_QUIET_TIME = TimeUnit.SECONDS.toNanos(1);   //默认超过1s没有任务进入队列就表示可以安全结束, 其他交由rejectedHandler处理

    private static final int S_UNSTART = 1;
    private static final int S_STARTED = 2;
    private static final int S_SHUTTING = 3;
    private static final int S_SHUTDOWN = 4;
    private static final int S_TERMINATED = 5;
    
    private final static Runnable WAKEUP_TASK = ()->{};
    
    private final RejectedHandler rejectedHandler;
    
    private final AtomicInteger state = new AtomicInteger(S_UNSTART);
    
    private final BlockingQueue<Runnable> taskQueue;
    
    private final CountDownLatch threadLock = new CountDownLatch(1);
    
    private volatile Thread thread;

    private long lastExecutionTime;
    private long shutdownStartTime;
    
    private volatile long shutdownTimeoutTime;
    private volatile long shutdownQuietTime;
    
    public SingleThreadExecutor(ThreadFactory threadFactory, BlockingQueue<Runnable> taskQueue) {
        this(threadFactory, taskQueue, DISCARD);//
    }
    public SingleThreadExecutor(ThreadFactory threadFactory, BlockingQueue<Runnable> taskQueue, RejectedHandler rejectedHandler) {
        this.taskQueue = taskQueue;
        this.rejectedHandler = rejectedHandler;
        this.thread = threadFactory.newThread(this::callRun);
    }
    
    @Override
    public void execute(Runnable task) {
        checkNotNull(task);
        startThread();
        if(!offerTask(task)) {
            rejectedHandler.rejected(task, this);
        }
    }

    private boolean offerTask(Runnable task) {
        if (isShutdown()) {
            return false;
        }
        return taskQueue.offer(task);
    }
    
    private Runnable takeTask() {
        Runnable task = null;
        try {
            task = taskQueue.take();
            if (task == WAKEUP_TASK) {
                task = null;
            }
        } catch (InterruptedException e) {
            // Ignore
        }
        return task;
    }

    private void checkNotNull(Runnable task) {
        if(task == null) {
            throw new NullPointerException("task");
        }
    }

    private void startThread() {
        if(state.get() == S_UNSTART) {
            if(state.compareAndSet(S_UNSTART, S_STARTED)) {
                thread.start();
            }
        }
    }

    void callRun() {
        try {
            run();
        } catch (Throwable t) {
            logger.warn("Unexpected exception from task executor: ", t);
        } finally {
            //结束run只能是shutdown, (已经shutdown, 或者变更成shuttingdown)
            for(;;) {
                int oldStatus = state.get();
                if(oldStatus >= S_SHUTTING || state.compareAndSet(oldStatus, S_SHUTTING)) {
                    break;
                }
            }
            
            try {
                for(;;) {
                    if(confirmShutdown()) {
                        break;
                    }
                }
            } finally {
                state.set(S_TERMINATED);
                if(!taskQueue.isEmpty()) {
                    logger.warn("Executor terminated with non-empty task queue (" + taskQueue.size() + ")");
                    runAllTasks();//offerTask已经对外关闭, 继续把不应该出现的任务执行完成
                }
                //implemention [wakeup termination await] where
                threadLock.countDown();
            }
        }
    }
    
    void run() {
        for (;;) {
            Runnable task = takeTask();
            if (task != null) {
                task.run();
                updateLastExecutionTime();
            }
            if (confirmShutdown()) {
                break;
            }
        }
    }

    private boolean confirmShutdown() {
        if(!isShuttingDown()) {
            return false;
        }
        if(!inExecThread()) {
            throw new IllegalStateException("confirmShutdown must be invoked by the executor thread");
        }
        //正在shutting down, 执行shuttingdown逻辑
        if(shutdownStartTime == 0) {
            shutdownStartTime = System.nanoTime();
        }
        
        if(runAllTasks()) {//有超过一个任务被执行返回true
            if(isShutdown()) {
                return true;
            }
            wakeup(true);//唤醒继续尝试执行后续放入的任务(可能没有)
            return false;
        }
        
        final long nanoTime = System.nanoTime();
        if(isShutdown() || nanoTime - shutdownStartTime > shutdownTimeoutTime) {
            return true;
        }
        
        if(nanoTime - lastExecutionTime < shutdownQuietTime) {//上次task.run小于2s, 尝试等待新任务, 每100ms唤醒一次
            wakeup(true);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //ignore
            }
            return false;
        }
        //超过quiet time没有任务进入队列, 可以认为结束线程是比较安全的了
        return true;
    }

    private void wakeup(boolean inExecThread) {
        if(!inExecThread || state.get() == S_SHUTTING) {
            taskQueue.offer(WAKEUP_TASK);
        }
    }

    private boolean runAllTasks() {
        Runnable task = pollTask();
        if (task == null) {
            return false;
        }
        for(;;) {
            try {
                task.run();
            } catch (Throwable t) {
                logger.warn("A task raised an exception.", t);
            }
            task = pollTask();
            if(task == null) {
                break;
            }
        }
        updateLastExecutionTime();
        return true;
    }

    private Runnable pollTask() {
        for (;;) {
            Runnable task = taskQueue.poll();
            if (task == WAKEUP_TASK) {
                continue;
            }
            return task;
        }
    }

    private boolean isShuttingDown() {
        return state.get() >= S_SHUTTING;
    }

    private void updateLastExecutionTime() {
        lastExecutionTime = System.nanoTime();
    }

    @Override
    public void shutdown() {
        shutdown(DEF_SHUTDOWN_TIMEOUT_TIME, DEF_SHUTDOWN_QUIET_TIME);
    }
    public void shutdown(long shutdownTimeoutTime, long shutdownQuietTime) {
        if (isShuttingDown()) {
            return;
        }
        this.shutdownTimeoutTime = shutdownTimeoutTime;
        this.shutdownQuietTime = shutdownQuietTime;

        boolean inExecThread = inExecThread();
        boolean wakeup;
        int oldState;
        for (;;) {
            if (isShuttingDown()) {
                return;
            }
            int newState;
            wakeup = true;
            oldState = state.get();
            if (inExecThread) {
                newState = S_SHUTTING;
            } else {
                switch (oldState) {
                    case S_UNSTART:
                    case S_STARTED:
                        newState = S_SHUTTING;
                        break;
                    default:
                        newState = oldState;
                        wakeup = false;
                }
            }
            if (state.compareAndSet(oldState, newState)) {
                break;
            }
        }
        if (oldState == S_UNSTART) {
            thread.start();
        }

        if (wakeup) {
            wakeup(inExecThread);
        }
        return;
    }

    private boolean inExecThread() {
        return Thread.currentThread() == thread;
    }

    @Override
    public boolean isShutdown() {
        return state.get() >= S_SHUTDOWN;
    }
    
    @Override
    public boolean isTerminated() {
        return state.get() >= S_TERMINATED;
    }
    
    @Override
    public List<Runnable> shutdownNow() {//与shutdown不同的是直接设置的状态为S_SHUTDOWN offerTask会被reject
        if (isShutdown()) {
            return Collections.emptyList();
        }

        boolean inExecThread = inExecThread();
        boolean wakeup;
        int oldState;
        for (;;) {
            if (isShuttingDown()) {
                return Collections.emptyList();
            }
            int newState;
            wakeup = true;
            oldState = state.get();
            if (inExecThread) {
                newState = S_SHUTDOWN;
            } else {
                switch (oldState) {
                    case S_UNSTART:
                    case S_STARTED:
                    case S_SHUTTING:
                        newState = S_SHUTDOWN;
                        break;
                    default:
                        newState = oldState;
                        wakeup = false;
                }
            }
            if (state.compareAndSet(oldState, newState)) {
                break;
            }
        }

        if (oldState == S_UNSTART) {
            thread.start();
        }

        if (wakeup) {
            wakeup(inExecThread);
        }
        return Collections.emptyList();
    }

    public BlockingQueue<Runnable> getQueue() {
		return taskQueue;
	}
    
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (inExecThread()) {
            throw new IllegalStateException("Cannot await termination in worker thread");
        }
        threadLock.await(timeout, unit);
        return isTerminated();
    }

}

package dev.xframe.action;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Future action
 * Method await for the action exec done
 * @author luzj
 *
 * @param <V>
 */
public abstract class FutureAction extends Action {
    
    private CountDownLatch latch;
    private AtomicInteger status;
    
    volatile Throwable failure;
    
    static final int STATUS_NORMAL   = 1;
    static final int STATUS_RUNNING  = 2;
    static final int STATUS_CANCEL   = 3;
    static final int STATUS_COMPLETE = 4;

    public FutureAction(ActionLoop loop) {
        super(loop);
        this.status = new AtomicInteger(STATUS_NORMAL);
        this.latch = new CountDownLatch(1);
    }

    public boolean cancel() {
        if(status.compareAndSet(STATUS_NORMAL, STATUS_CANCEL)) {
            latch.countDown();
            return true;
        }
        return false;
    }

    public boolean isCancelled() {
        return status.get() == STATUS_CANCEL;
    }
    
    protected void done() {
        if(status.compareAndSet(STATUS_RUNNING, STATUS_COMPLETE)) {
            latch.countDown();
        }
    }
    
    public boolean isDone() {
        int st = status.get();
        return st == STATUS_COMPLETE || st == STATUS_CANCEL;
    }

    public void await() throws InterruptedException, ExecutionException {
        latch.await();
        
        this.report();
    }

    private void report() throws ExecutionException {
        if(status.get() == STATUS_CANCEL) {
            throw new CancellationException();
        } 
        if (failure != null) {
            throw new ExecutionException(failure);
        }
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        if (!latch.await(timeout, unit)) {
            throw new TimeoutException();
        }
        this.report();
    }
    
    protected void failure(Throwable failure) {
        this.failure = failure;
    }
    
    protected boolean runable() {
        return status.compareAndSet(STATUS_NORMAL, STATUS_RUNNING);
    }
    
    public static final FutureAction of(ActionLoop loop, Runnable runnable) {
        return new FutureAction(loop) {protected void exec() {runnable.run();}};
    }
    
}
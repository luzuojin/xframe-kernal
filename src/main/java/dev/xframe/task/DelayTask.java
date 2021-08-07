package dev.xframe.task;

import java.util.concurrent.TimeUnit;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public abstract class DelayTask extends Task implements TimerTask {

    protected int delayMillis;

    volatile boolean isCancelled;

    public DelayTask(TaskLoop loop, int delay) {
        super(loop);
        this.reset(createTime, delay);
    }

    @Override
    protected boolean runable() {
        return !isCancelled;
    }

    private void reset(long curTime, int delay) {
        this.isCancelled = false;
        this.createTime  = curTime;
        this.delayMillis = delay;
    }

    @Override
    public void checkin() {
        if(this.delayMillis > 0) {
            loop.schedule(this);
        } else {//don`t need delay
            loop.checkin(this);
        }
    }

    public void cancel() {
        this.isCancelled = true;
    }

    public void recheckin(int delay) {
        this.recheckin(System.currentTimeMillis(), delay);
    }

    public void recheckin(long curTime, int delay) {
        reset(curTime, delay);
        checkin();
    }

    //timeout
    @Override
    public final void run(Timeout timeout) throws Exception {
        if(isCancelled || timeout.isCancelled())
            return;
        //checkin as simple task
        createTime = createTime + delayMillis;
        loop.checkin(this);
    }

    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(this.delayMillis, TimeUnit.MILLISECONDS));
    }

    public static final DelayTask of(TaskLoop loop, int delay, Runnable runnable) {
        return new DelayTask(loop, delay) {
            protected void exec() {
                runnable.run();
            }
            protected Class<?> getClazz() {
                return runnable.getClass();
            }
        };
    }

}

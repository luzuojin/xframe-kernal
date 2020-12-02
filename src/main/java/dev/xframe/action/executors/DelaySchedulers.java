package dev.xframe.action.executors;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.action.ActionExecutor;
import dev.xframe.action.DelayAction;
import dev.xframe.utils.XProperties;
import dev.xframe.utils.XThreadFactory;
import io.netty.util.HashedWheelTimer;

public class DelaySchedulers {
    static DelayScheduler hashedwheel;//hashedwheel全局只用一个实例
    static boolean useHashedWheel = XProperties.getAsBool("xframe.hashedwheel", true);
    static int tickDuration = XProperties.getAsInt("xframe.hashedwheel.tickduration", 100);
    
    static synchronized DelayScheduler make(String name) {
        if(useHashedWheel) {
            if(hashedwheel == null) {
                hashedwheel = new HwtDelayScheduler(tickDuration);
                hashedwheel.startup();
            }
            return hashedwheel;
        } else {
            DelayScheduler s = new SimpleDelayScheduler(name);
            s.startup();
            return s;
        }
    }
}

class HwtDelayScheduler implements DelayScheduler {
    final static TimeUnit unit = TimeUnit.MILLISECONDS;
    final HashedWheelTimer timer;
    public HwtDelayScheduler(int tickDuration) {
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


class SimpleDelayScheduler extends Thread implements DelayScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
    private DelayQueue<DelayAction> queue;
    private volatile boolean isRunning;
    private int checkedCount;

    public SimpleDelayScheduler(String prefix) {
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
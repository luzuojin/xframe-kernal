package dev.xframe.action;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.metric.Metric;
import dev.xframe.metric.Metrical;

public abstract class Action implements Runnable, Metrical {
    
    protected static final Logger logger = LoggerFactory.getLogger(Action.class);
    
    protected ActionQueue queue;
    protected long createTime;

    public Action(ActionQueue queue) {
        this.queue = queue;
        this.createTime = System.currentTimeMillis();
    }

    public ActionQueue getActionQueue() {
        return queue;
    }
    
    public void checkin() {
        this.queue.checkin(this);
    }
    
    @Override
    public final void run() {
        try {
        	ActionQueue.current.set(queue);
            if(runable()) {
                long createTime = this.createTime;
                long start = System.currentTimeMillis();
                this.exec();
                long end = System.currentTimeMillis();
                Metric.gauge(getClazz(), createTime, start, end, this);
            }
        } catch (Throwable e) {
            logger.error("Execute exception: " + getClazz().getName(), e);
            failure(e);
        } finally {
        	ActionQueue.current.remove();
            queue.checkout(this);
            done();
        }
    }
    
    public final void execute() {
        this.exec();
    }
    
    protected boolean runable() {
        return true;
    }

    protected void done() {
        //do nothing
    }
    
    protected void failure(Throwable e) {
        //do nothing
    }

    protected abstract void exec();
    
    protected Class<?> getClazz() {
    	return this.getClass();
    }
    
	public final int waitings() {
		return queue.size();
	}

	@Override
    public String toString() {
        return getClazz().getName() + "[" + DateTimeFormatter.ofPattern("HH:mm:ss").format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(createTime), ZoneOffset.systemDefault())) + "]";
    }

}

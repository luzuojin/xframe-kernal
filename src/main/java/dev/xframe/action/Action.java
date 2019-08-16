package dev.xframe.action;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.metric.Metrics;

public abstract class Action implements Runnable {
    
    protected static final Logger logger = LoggerFactory.getLogger(Action.class);
    
    protected ActionLoop loop;
    protected long createTime;

    public Action(ActionLoop loop) {
        this.loop = loop;
        this.createTime = System.currentTimeMillis();
    }

    public void checkin() {
        this.loop.checkin(this);
    }
    
    public long getCreateTime() {
		return createTime;
	}

	@Override
    public final void run() {
        try {
        	ActionLoop.setCurrent(loop);
            if(runable()) {
                long create = this.createTime;
                long start = System.currentTimeMillis();
                this.exec();
                long end = System.currentTimeMillis();
                Metrics.gauge(getClazz(), create, start, end, this);
            }
        } catch (Throwable e) {
            logger.error("Execute exception: " + getClazz().getName(), e);
            failure(e);
        } finally {
        	ActionLoop.unsetCurrent();
            loop.checkout(this);
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
    
	public final int loopings() {
		return loop.size();
	}

	@Override
    public String toString() {
        return getClazz().getName() + "[" + DateTimeFormatter.ofPattern("HH:mm:ss").format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(createTime), ZoneOffset.systemDefault())) + "]";
    }

}

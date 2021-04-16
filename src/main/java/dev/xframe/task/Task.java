package dev.xframe.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.metric.Gauge;
import dev.xframe.utils.XDateFormatter;

public abstract class Task implements Runnable {
    
    protected static final Logger logger = LoggerFactory.getLogger(Task.class);
    
    protected TaskLoop loop;
    protected long createTime;

    public Task(TaskLoop loop) {
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
        	TaskLoop.setCurrent(loop);
            if(runable()) {
            	Gauge g = Gauge.of(getName()).create(createTime).begin();
                this.exec();
                g.end().apply();
            }
        } catch (Throwable e) {
            logger.error("Execute exception: " + getName(), e);
            failure(e);
        } finally {
            done();
            TaskLoop.unsetCurrent();
        	loop.checkout(this);
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
    protected String getName() {
		return getClazz().getName();
	}
    
	@Override
    public String toString() {
        return getName() + "[" + XDateFormatter.from(createTime) + "]";
    }

}

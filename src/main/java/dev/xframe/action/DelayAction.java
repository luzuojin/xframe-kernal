package dev.xframe.action;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class DelayAction extends Action implements Delayed {
    
    long execTime;
    
    volatile boolean isCancelled;
    
	public DelayAction(ActionLoop loop, int delay) {
	    super(loop);
	    this.initial(createTime, delay);
	}
	
	public DelayAction(ActionLoop loop, long curTime, int delay) {
		super(loop);
		this.initial(curTime, delay);
	}

	@Override
    protected boolean runable() {
	    return !isCancelled;
    }

    private void initial(long curTime, int delay) {
        this.isCancelled = false;
        this.createTime = curTime;
        this.execTime = delay > 0 ? (curTime + delay) : 0;
	}

	@Override
    public void checkin() {
	    if(this.execTime == 0) {//don`t need delay
	        loop.checkin(this);
	    } else {
	        loop.schedule(this);
	    }
    }
	
	public void cancel() {
	    this.isCancelled = true;
	}
	
	public void recheckin(int delay) {
		this.recheckin(System.currentTimeMillis(), delay);
	}
	
	public void recheckin(long curTime, int delay) {
		initial(curTime, delay);
		checkin();
	}

    public boolean tryExec(long curTime) {
        if(isCancelled) {
            return true;
        }
        
		if(curTime >= execTime) {
			createTime = curTime;
			getActionLoop().checkin(this);
			return true;
		}
		return false;
	}
    
    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.execTime, ((DelayAction)o).execTime);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.execTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String toString() {
        return getClazz().getName() + "[" + DateTimeFormatter.ofPattern("HH:mm:ss").format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(execTime), ZoneOffset.systemDefault())) + "]";
    }  

    public static final DelayAction of(ActionLoop loop, int delay, Runnable runnable) {
        return new DelayAction(loop, delay) {protected void exec() {runnable.run();}};
    }
    
}

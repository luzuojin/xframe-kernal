package dev.xframe.action;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import dev.xframe.utils.XDateFormatter;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public abstract class DelayAction extends Action implements Delayed, TimerTask {
    
    protected long execTime;
    
    volatile boolean isCancelled;
    
	public DelayAction(ActionLoop loop, int delay) {
	    super(loop);
	    this.reset(createTime, delay);
	}

	@Override
    protected boolean runable() {
	    return !isCancelled;
    }

    private void reset(long curTime, int delay) {
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
		reset(curTime, delay);
		checkin();
	}

	//timeout
    @Override
	public final void run(Timeout timeout) throws Exception {
    	if(isCancelled || timeout.isCancelled())
    		return;
    	//checkin as action
    	createTime = execTime;
    	loop.checkin(this);
	}

	public boolean tryExec(long curTime) {
        if(isCancelled) {
            return true;
        }
        
		if(curTime >= execTime) {
			createTime = curTime;
			loop.checkin(this);
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
        return getName() + "[" + XDateFormatter.from(execTime) + "]";
    }

    public static final DelayAction of(ActionLoop loop, int delay, Runnable runnable) {
		return new DelayAction(loop, delay) {
			protected void exec() {
				runnable.run();
			}
			protected Class<?> getClazz() {
				return runnable.getClass();
			}
		};
    }
    
}

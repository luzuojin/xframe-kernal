package dev.xframe.task.scheduled;

import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import dev.xframe.task.DelayTask;
import dev.xframe.task.RunnableTask;
import dev.xframe.task.TaskLoop;
import dev.xframe.utils.XLogger;

public class ScheduledFlux implements Runnable {
	
	private PriorityQueue<Unit> units = new PriorityQueue<>();
	
	private TaskLoop loop;
	
	public ScheduledFlux(TaskLoop loop) {
	    this.loop = loop;
	}
	
	public void regist(Runnable task, int delay) {
	    if(!loop.inLoop()) {//can`t be here
            XLogger.warn("Flux non loopped regist");
	        RunnableTask.of(loop, ()->doRegist(task, delay)).checkin();
	    } else {
	        doRegist(task, delay);
	    }
	}
	
    private void doRegist(Runnable task, int delay) {
        Unit unit = new Unit(task, System.currentTimeMillis() + delay);
        units.add(unit);
        tryCheckin();
    }
    
    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for(;;) {
            Unit next = units.peek();
            if(next == null || next.execTime > now) {
                break;
            }
            try {
                next.runner.run();
            } catch (Throwable e) {
                XLogger.warn("Flux task throws:", e);
            } finally {
                units.poll();
            }
        }
        tryCheckin();
    }
    
    public void clear() {
        if(!loop.inLoop()) {//can`t be here
        	XLogger.warn("Flux non loopped clear");
            RunnableTask.of(loop, ()->doClear()).checkin();
        } else {
            doClear();
        }
    }

    private void doClear() {
        units.clear();
    }
    
	private void tryCheckin() {
	    tryCheckin(units.peek());
    }
	
	private void tryCheckin(Unit unit) {
        if(unit != null && !unit.checked) {
            unit.checked = true;
            int delay = (int) unit.getDelay(TimeUnit.MILLISECONDS);
            DelayTask.of(loop, delay, this).checkin();;
        }
	}

	static class Unit implements Delayed {
		final Runnable runner;
		final long execTime;
		boolean checked = false;
		public Unit(Runnable runner, long execTime) {
			this.runner = runner;
			this.execTime = execTime;
		}
		@Override
		public int compareTo(Delayed o) {
			return Long.compare(execTime, ((Unit) o).execTime);
		}
		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(this.execTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}

}

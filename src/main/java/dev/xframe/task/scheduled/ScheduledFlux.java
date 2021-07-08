package dev.xframe.task.scheduled;

import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import dev.xframe.task.DelayTask;
import dev.xframe.task.RunnableTask;
import dev.xframe.task.TaskLoop;
import dev.xframe.utils.XLogger;

/**
 * 多个ScheduledTask包装类. 
 * @author luzj
 */
public class ScheduledFlux implements Runnable {
    private static long StartTime = System.nanoTime();
    private static long calcDeadline(int delay) {   //计划执行时间
        return System.nanoTime() - StartTime + TimeUnit.MILLISECONDS.toNanos(delay);
    }
    private static long calcBaseline() {            //当前时间
        return System.nanoTime() - StartTime;
    }
	
	private PriorityQueue<SFUnit> units = new PriorityQueue<>();
	
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
        SFUnit unit = new SFUnit(task, calcDeadline(delay));
        units.add(unit);
        tryCheckin();
    }

    @Override
    public void run() {
        long now = calcBaseline();
        for(;;) {
            SFUnit next = units.peek();
            if(next == null || next.deadline > now) {
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
	
	private void tryCheckin(SFUnit unit) {
        if(unit != null && !unit.checked) {
            unit.checked = true;
            int delay = (int) unit.getDelay(TimeUnit.MILLISECONDS);
            DelayTask.of(loop, delay, this).checkin();
        }
	}

	static class SFUnit implements Delayed {
		final Runnable runner;
		final long deadline;
		boolean checked = false;
		public SFUnit(Runnable runner, long deadline) {
			this.runner = runner;
			this.deadline = deadline;
		}
		@Override
		public int compareTo(Delayed o) {
			return Long.compare(deadline, ((SFUnit) o).deadline);
		}
		@Override
		public long getDelay(TimeUnit unit) {
			return Math.max(0, unit.convert(this.deadline - calcBaseline(), TimeUnit.NANOSECONDS));
		}
	}
}

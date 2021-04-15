package dev.xframe.action.scheduled;

import java.util.concurrent.TimeUnit;

import dev.xframe.action.DelayAction;
import dev.xframe.utils.XProperties;
import dev.xframe.utils.XThreadFactory;
import io.netty.util.HashedWheelTimer;

public class ScheduledExecutor {
	
	private static final TimeUnit TickUnit = TimeUnit.MILLISECONDS;
	
	private static final long TickDuration = XProperties.getAsLong("xframe.tickduration", 100);
	
	private static final HashedWheelTimer _Timer = new HashedWheelTimer(new XThreadFactory("Timer"), TickDuration, TickUnit);
	
	public void checkin(DelayAction task) {
		_Timer.newTimeout(task, task.getDelay(TickUnit), TickUnit);
	}
	
	public void shutdown() {
		_Timer.stop();
	}

}

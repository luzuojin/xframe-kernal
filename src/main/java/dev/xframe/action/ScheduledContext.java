package dev.xframe.action;

import dev.xframe.action.scheduled.ScheduledAction;
import dev.xframe.inject.Configurator;
import dev.xframe.utils.XProperties;

@Configurator
public class ScheduledContext {

	private ActionExecutor executor = ActionExecutors.newFixed("scheduled", scheduledThreads());
	
	private ActionLoop loop = new ActionLoop.Direct(executor);

	private int scheduledThreads() {
		int def = Math.min(1, Runtime.getRuntime().availableProcessors() / 2);
		return XProperties.getAsInt("xframe.scheduledthreads", def);
	}
	
	public ActionLoop loop() {
		return loop;
	}
	
	public void once(Runnable task, int delay) {
		ScheduledAction.once(loop(), delay, task).checkin();
	}
	
	public void period(Runnable task, int period) {
		ScheduledAction.period(loop, period, task).checkin();
	}
	
	public void period(Runnable task, int delay, int period) {
		ScheduledAction.period(loop, delay, period, task).checkin();
	}
	
}

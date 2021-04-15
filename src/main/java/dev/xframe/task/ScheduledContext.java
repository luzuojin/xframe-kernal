package dev.xframe.task;

import dev.xframe.inject.Configurator;
import dev.xframe.task.scheduled.ScheduledTask;
import dev.xframe.utils.XProperties;

@Configurator
public class ScheduledContext {

	private TaskExecutor executor = TaskExecutors.newFixed("scheduled", scheduledThreads());
	
	private TaskLoop loop = new TaskLoop.Direct(executor);

	private int scheduledThreads() {
		int def = Math.min(1, Runtime.getRuntime().availableProcessors() / 2);
		return XProperties.getAsInt("xframe.scheduledthreads", def);
	}
	
	public TaskLoop loop() {
		return loop;
	}
	
	public void once(Runnable task, int delay) {
		ScheduledTask.once(loop(), delay, task).checkin();
	}
	
	public void period(Runnable task, int period) {
		ScheduledTask.period(loop, period, task).checkin();
	}
	
	public void period(Runnable task, int delay, int period) {
		ScheduledTask.period(loop, delay, period, task).checkin();
	}
	
}

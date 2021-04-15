package dev.xframe.task;

import java.util.concurrent.atomic.AtomicInteger;

import dev.xframe.inject.Configurator;
import dev.xframe.task.scheduled.ScheduledTask;
import dev.xframe.utils.XProperties;

@Configurator
public class ScheduledContext {
	
    private AtomicInteger running = new AtomicInteger(0);

	private TaskExecutor executor;
	
	private TaskLoop loop;
	
	private void ensure() {
		if(running.get() == 0 && running.compareAndSet(0, 1)) {
			executor = TaskExecutors.newFixed("scheduled", threads());
			loop = new TaskLoop.Direct(executor);
		}
	}
	private int threads() {
		int def = Math.min(1, Runtime.getRuntime().availableProcessors() / 2);
		return XProperties.getAsInt("xframe.scheduledthreads", def);
	}
	
	public TaskLoop loop() {
		this.ensure();
		return loop;
	}
	
	public void once(Runnable task, int delay) {
		ScheduledTask.once(loop(), delay, task).checkin();
	}
	
	public void period(Runnable task, int period) {
		ScheduledTask.period(loop(), period, task).checkin();
	}
	
	public void period(Runnable task, int delay, int period) {
		ScheduledTask.period(loop(), delay, period, task).checkin();
	}
	
}

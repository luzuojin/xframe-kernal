package dev.xframe.task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.xframe.inject.Bean;
import dev.xframe.task.scheduled.ScheduledTask;
import dev.xframe.utils.XProperties;

@Bean
public class ScheduledContext {

    private AtomicBoolean running = new AtomicBoolean(false);

    private TaskExecutor executor;

    private TaskLoop loop;

    private void ensure() {
        if(running.get() && running.compareAndSet(false, true)) {
            executor = TaskExecutors.newFixed("scheduled", threads());
            loop = new TaskLoop.Direct(executor);
        }
    }
    private int threads() {
        return XProperties.getAsInt("xframe.scheduled.threads", 1);
    }

    public TaskLoop loop() {
        this.ensure();
        return loop;
    }

    public void once(Runnable task, int delay) {
        period(task, delay, -1);
    }
    public void once(Runnable task, int delay, TimeUnit unit) {
        period(task, delay, -1, unit);
    }

    public void period(Runnable task, int period) {
        period(task, period, period);
    }
    public void period(Runnable task, int period, TimeUnit unit) {
        period(task, period, period, unit);
    }

    public void period(Runnable task, int delay, int period) {
        period(task, delay, period, TimeUnit.MILLISECONDS);
    }
    public void period(Runnable task, int delay, int period, TimeUnit unit) {
        int _delay = delay > 0 ? (int) TimeUnit.MILLISECONDS.convert(delay, unit) : delay;
        int _period = period > 0 ? (int) TimeUnit.MILLISECONDS.convert(period, unit) : period;
        ScheduledTask.period(loop(), _delay, _period, task).checkin();
    }

}

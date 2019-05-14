package dev.xframe.task;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class TaskRegistator implements ScheduledFuture<Object> {
	
	private boolean cancel;
	private boolean done;
	
	private Task task;
	
	public TaskRegistator(Task task) {
		this.task = task;
	}
	
	public void registTo(TaskContext taskCtx) {
	    taskCtx.regist(task);
		done = true;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Delayed o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		this.cancel = true;
		return true;
	}

	@Override
	public boolean isCancelled() {
		return this.cancel;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

}

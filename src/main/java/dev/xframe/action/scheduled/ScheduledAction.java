package dev.xframe.action.scheduled;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import dev.xframe.action.ActionLoop;
import dev.xframe.action.DelayAction;
import dev.xframe.utils.XLambda;

public abstract class ScheduledAction extends DelayAction {
	
	protected final int period;
	
	public ScheduledAction(ActionLoop loop, int delay, int period) {
		super(loop, delay);
		this.period = period;
	}
	
	@Override
	protected void done() {
		if(period > 0) {
			recheckin(period);
		}
	}
	
	public static ScheduledAction once(ActionLoop loop, int delay, Runnable runnable) {
		return period(loop, delay, -1, runnable);
	}
	
	public static ScheduledAction period(ActionLoop loop, int period, Runnable runnable) {
		return period(loop, period, period, runnable);
	}
	public static ScheduledAction period(ActionLoop loop, int delay, int period, Runnable runnable) {
		return new Simple(loop, delay, period, runnable);
	}
	
	public static final class Simple extends ScheduledAction {
		final Runnable runnable;
		public Simple(ActionLoop loop, int delay, int period, Runnable runnable) {
			super(loop, delay, period);
			this.runnable = runnable;
		}
		protected void exec() {
			runnable.run();
		}
		protected Class<?> getClazz() {
			return runnable.getClass();
		}
	}
	
	public static class MethodBased extends ScheduledAction {
		protected final String name;
		protected final Object delegate;
		protected final Consumer<Object> runner;
		@SuppressWarnings("unchecked")
		public MethodBased(String name, ActionLoop loop, int delay, int period, Object delegate, Method method) {
			super(loop, delay, period);
			this.name = name;
			this.delegate = delegate;
			this.runner = XLambda.create(Consumer.class, method);
		}
		protected void exec() {
			runner.accept(delegate);
		}
		protected String getName() {
			return name;
		}
	}
}

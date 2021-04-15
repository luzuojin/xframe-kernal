package dev.xframe.action.scheduled;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dev.xframe.action.ActionLoop;

public class ScheduledBuilder {
	
	public static List<ScheduledAction> newTasks(ActionLoop loop, Object delegate) {
		return newTasks(loop, delegate.getClass(), delegate);
	}
	public static List<ScheduledAction> newTasks(ActionLoop loop, Class<?> cls, Object delegate) {
		return newTasks0(new ArrayList<>(), loop, cls, delegate);
	}
	private static List<ScheduledAction> newTasks0(List<ScheduledAction> sts, ActionLoop loop, Class<?> cls, Object delegate) {
		if(cls != Object.class) {
			newTasks0(sts, loop, cls.getSuperclass(), delegate);
			//make task
			newTasks1(sts, loop, cls, delegate);
		}
		return sts;
	}
	private static void newTasks1(List<ScheduledAction> sts, ActionLoop loop, Class<?> cls, Object delegate) {
		Method[] methods = cls.getDeclaredMethods();
		for (Method method : methods) {
			if(method.isAnnotationPresent(Scheduled.class)) {
				sts.add(newTask(loop, method, delegate));
			}
		}
	}
	@SuppressWarnings("unchecked")
	private static ScheduledAction newTask(ActionLoop loop, Method scheduledMethod, Object delegate) {
		int delay = getDelay(scheduledMethod);
		int period = getPeriod(scheduledMethod);
		String name = getName(scheduledMethod);
		return new ScheduledAction.MethodBased(name, loop, delay, period, delegate, scheduledMethod);
	}
	private static String getName(Method scheduledMethod) {
		return scheduledMethod.getDeclaringClass().getSimpleName() + "." + scheduledMethod.getName();
	}
	private static int getDelay(Method scheduledMethod) {
		Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);
		int delay = scheduled.delay();
		if(delay == 0) {
			delay = scheduled.period();
		}
		return delay;
	}
	private static int getPeriod(Method scheduledMethod) {
		Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);
		return scheduled.period();
	}
}

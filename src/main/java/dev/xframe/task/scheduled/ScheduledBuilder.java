package dev.xframe.task.scheduled;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.xframe.task.TaskLoop;

public class ScheduledBuilder {
	
	public static List<ScheduledTask> newTasks(TaskLoop loop, Object delegate) {
		return newTasks(loop, delegate.getClass(), delegate);
	}
	public static List<ScheduledTask> newTasks(TaskLoop loop, Class<?> cls, Object delegate) {
		return newTasks0(new ArrayList<>(), loop, cls, delegate);
	}
	private static List<ScheduledTask> newTasks0(List<ScheduledTask> sts, TaskLoop loop, Class<?> cls, Object delegate) {
		if(cls != Object.class) {
			newTasks0(sts, loop, cls.getSuperclass(), delegate);
			//make task
			newTasks1(sts, loop, cls, delegate);
		}
		return sts;
	}
	private static void newTasks1(List<ScheduledTask> sts, TaskLoop loop, Class<?> cls, Object delegate) {
		Method[] methods = cls.getDeclaredMethods();
		for (Method method : methods) {
			if(method.isAnnotationPresent(Scheduled.class)) {
				sts.add(newTask(loop, method, delegate));
			}
		}
	}
	@SuppressWarnings("unchecked")
	private static ScheduledTask newTask(TaskLoop loop, Method scheduledMethod, Object delegate) {
		int delay = getDelay(scheduledMethod);
		int period = getPeriod(scheduledMethod);
		String name = getName(scheduledMethod);
		return new ScheduledTask.MethodBased(name, loop, delay, period, delegate, scheduledMethod);
	}
	private static String getName(Method scheduledMethod) {
		return scheduledMethod.getDeclaringClass().getSimpleName() + "." + scheduledMethod.getName();
	}
	private static int getDelay(Method scheduledMethod) {
		Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);
		if(scheduled.dailyOffset() > 0) {//daily
			long now = System.currentTimeMillis();
			long off = scheduled.dailyOffset() - (now - TimeUnit.MILLISECONDS.toDays(now));
			long dly = off > 0 ? off : TimeUnit.DAYS.toMillis(1) + off;//off < 0
			return (int) dly;
		}
		if(scheduled.delay() > 0) {
			return scheduled.delay();
		}
		return scheduled.period();
	}
	private static int getPeriod(Method scheduledMethod) {
		Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);
		if(scheduled.dailyOffset() > 0) {//daily
			return (int) TimeUnit.DAYS.toMillis(1);
		}
		return scheduled.period();
	}
}

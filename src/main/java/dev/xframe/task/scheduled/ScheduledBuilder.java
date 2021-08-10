package dev.xframe.task.scheduled;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    private static ScheduledTask newTask(TaskLoop loop, Method scheduledMethod, Object delegate) {
        int delay = getDelayMillis(scheduledMethod);
        int period = getPeriodMillis(scheduledMethod);
        String name = getScheduledName(scheduledMethod);
        return new ScheduledTask.MethodBased(name, loop, delay, period, delegate, scheduledMethod);
    }
    private static String getScheduledName(Method scheduledMethod) {
        return scheduledMethod.getDeclaringClass().getSimpleName() + "." + scheduledMethod.getName();
    }
    private static int getDelayMillis(Method scheduledMethod) {
        Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);
        int delayMillis = scheduled.baseline().delayCaculator.applyAsInt(scheduled.unit().toMillis(scheduled.delay()));
        //fixedRate, 启动时跳过第一个周期
        if(delayMillis == 0 && scheduled.baseline() == Scheduled.Baseline.CURRENT) {
            delayMillis = getPeriodMillis(scheduledMethod);
        }
        return delayMillis;
    }
    private static int getPeriodMillis(Method scheduledMethod) {
        Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);
        return scheduled.period() == -1 ? scheduled.baseline().period : (int) scheduled.unit().toMillis(scheduled.period());
    }
}

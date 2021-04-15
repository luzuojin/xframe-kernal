package dev.xframe.test;

import org.junit.Ignore;

import dev.xframe.task.DelayTask;
import dev.xframe.task.Task;
import dev.xframe.task.TaskExecutor;
import dev.xframe.task.TaskExecutors;
import dev.xframe.task.TaskLoop;

@Ignore
public class TaskLoopTest {
    
    public static void main(String[] args) {
        TaskExecutor executor = TaskExecutors.newFixed("Test", 2);
        for (int i = 10; i < 20; i++) {
            TaskLoop loop = executor.newLoop();
            final int j = i;
            (new DelayTask(loop, i*10) {
                @Override
                protected void exec() {
                    System.out.println(Thread.currentThread().getName() + "\t" + j);
                }
            }).checkin();
        }
        for (int i = 0; i < 10; i++) {
            final int j = i;
            TaskLoop loop = executor.newLoop();
            (new Task(loop) {
                @Override
                protected void exec() {
                    System.out.println(Thread.currentThread().getName() + "\t" + j);
                }
            }).checkin();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

}

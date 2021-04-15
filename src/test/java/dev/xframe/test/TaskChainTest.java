package dev.xframe.test;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import dev.xframe.task.Task;
import dev.xframe.task.TaskChain;
import dev.xframe.task.TaskExecutor;
import dev.xframe.task.TaskExecutors;
import dev.xframe.task.TaskLoop;

@Ignore
public class TaskChainTest {
    
    public static void main(String[] args) throws InterruptedException {
        TaskExecutor executor = TaskExecutors.newFixed("T", 1);
        TaskLoop loop = executor.newLoop();
        TaskChain chain = new TaskChain();
        
        chain.append(new Task(loop) {
            @Override
            public void exec() {
                System.out.println("Ni Hao!!!");
            }
        });
        
        chain.append(new Task(loop) {
            @Override
            public void exec() {
                System.out.println("Say Hello!!!");
            }
        });
        chain.checkin();
        
        TimeUnit.MILLISECONDS.sleep(50);
        executor.shutdown();
    }

}

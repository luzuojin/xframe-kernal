package dev.xframe.test;

import org.junit.Ignore;

import dev.xframe.action.Action;
import dev.xframe.action.ActionExecutor;
import dev.xframe.action.ActionExecutors;
import dev.xframe.action.ActionLoop;

@Ignore
public class ActionLoopTest {
    
    public static void main(String[] args) {
        ActionExecutor executor = ActionExecutors.newFixed("Test", 2);
        for (int i = 0; i < 10; i++) {
            final int j = i;
            ActionLoop loop = executor.newLoop();
            (new Action(loop) {
                @Override
                protected void exec() {
                    System.out.println(Thread.currentThread().getName() + "\t" + j);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).checkin();
        }
        executor.shutdown();
    }

}

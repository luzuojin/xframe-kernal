package dev.xframe.test;

import org.junit.Ignore;

import dev.xframe.action.Action;
import dev.xframe.action.ActionExecutor;
import dev.xframe.action.ActionExecutors;
import dev.xframe.action.ActionLoop;
import dev.xframe.action.DelayAction;

@Ignore
public class ActionLoopTest {
    
    public static void main(String[] args) {
        ActionExecutor executor = ActionExecutors.newFixed("Test", 2);
        for (int i = 10; i < 20; i++) {
            ActionLoop loop = executor.newLoop();
            final int j = i;
            (new DelayAction(loop, i*10) {
                @Override
                protected void exec() {
                    System.out.println(Thread.currentThread().getName() + "\t" + j);
                }
            }).checkin();
        }
        for (int i = 0; i < 10; i++) {
            final int j = i;
            ActionLoop loop = executor.newLoop();
            (new Action(loop) {
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

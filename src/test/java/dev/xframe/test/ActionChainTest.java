package dev.xframe.test;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import dev.xframe.action.Action;
import dev.xframe.action.ActionChain;
import dev.xframe.action.ActionExecutor;
import dev.xframe.action.ActionExecutors;
import dev.xframe.action.ActionLoop;

@Ignore
public class ActionChainTest {
    
    public static void main(String[] args) throws InterruptedException {
        ActionExecutor executor = ActionExecutors.newFixed("T", 1);
        ActionLoop loop = new ActionLoop(executor);
        ActionChain chain = new ActionChain();
        
        chain.append(new Action(loop) {
            @Override
            public void exec() {
                System.out.println("Ni Hao!!!");
            }
        });
        
        chain.append(new Action(loop) {
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

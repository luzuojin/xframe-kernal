package dev.xframe.action;

import java.util.LinkedList;
import java.util.List;

import dev.xframe.action.executors.ShardingActionExecutor;
import dev.xframe.action.executors.ThreadPoolActionExecutor;

public class ActionExecutors {
    
    private final static List<ActionExecutor> executors = new LinkedList<ActionExecutor>();
    
    public static ActionExecutor newSingle(String name) {
        return hold(new ThreadPoolActionExecutor(1, name));
    }
    
    public static ActionExecutor newFixed(String name, int nThreads) {
        return hold(new ThreadPoolActionExecutor(nThreads, name));
    }
    
    /**
     * 每一个ActionLoop会绑定到一个固定的线程中
     * @param name
     * @param nThreads
     * @return
     */
    public static ActionExecutor newSharding(String name, int nThreads) {
        return hold(new ShardingActionExecutor(nThreads, name));
    }
    
    private static ActionExecutor hold(ActionExecutor executor) {
        executors.add(executor);
        return executor;
    }
    
    public static void shutdown() {
        for (ActionExecutor executor : executors) {
            executor.shutdown();
        }
    }
    
    public static List<ActionExecutor> getExecutors() {
    	return executors;
    }

}

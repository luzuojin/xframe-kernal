package dev.xframe.task;

import java.util.LinkedList;
import java.util.List;

import dev.xframe.task.executors.ShardingTaskExecutor;
import dev.xframe.task.executors.ThreadPoolTaskExecutor;

public class TaskExecutors {
    
    private final static List<TaskExecutor> executors = new LinkedList<TaskExecutor>();
    
    public static TaskExecutor newSingle(String name) {
        return hold(new ThreadPoolTaskExecutor(1, name) {
			public TaskLoop newLoop() {
				return new TaskLoop.Direct(this);
			}
        });
    }
    
    public static TaskExecutor newFixed(String name, int nThreads) {
        return hold(new ThreadPoolTaskExecutor(nThreads, name));
    }
    
    /**
     * 每一个TaskLoop会绑定到一个固定的线程中
     * @param name
     * @param nThreads
     * @return
     */
    public static TaskExecutor newSharding(String name, int nThreads) {
        return hold(new ShardingTaskExecutor(nThreads, name));
    }
    
    private static TaskExecutor hold(TaskExecutor executor) {
        synchronized (executors) {
            executors.add(executor);
        }
        return executor;
    }
    
    public static void shutdown() {
        synchronized (executors) {
            for (TaskExecutor executor : executors) {
                executor.shutdown();
            }
        }
    }
    
    public static List<TaskExecutor> getExecutors() {
    	return executors;
    }

}

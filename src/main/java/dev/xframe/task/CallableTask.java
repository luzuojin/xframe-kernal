package dev.xframe.task;

public abstract class CallableTask extends Task {

    private final Task callable;
    
    public CallableTask(TaskLoop loop, Task callable) {
        super(loop);
        this.callable = callable;
    }
    
    @Override
    public void done() {
        if(callable != null) callable.checkin();
    }
    
    public static final CallableTask of(TaskLoop loop, Runnable runnable, Task callable){
        return new CallableTask(loop, callable) {protected void exec() {runnable.run();}};
    }

}

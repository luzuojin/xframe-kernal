package dev.xframe.action;

public abstract class CallableAction extends Action {

    private final Action callable;
    
    public CallableAction(ActionQueue queue, Action callable) {
        super(queue);
        this.callable = callable;
    }
    
    @Override
    public void done() {
        if(callable != null) callable.checkin();
    }
    
    public static final CallableAction of(ActionQueue queue, Runnable runnable, Action callable){
        return new CallableAction(queue, callable) {protected void exec() {runnable.run();}};
    }

}

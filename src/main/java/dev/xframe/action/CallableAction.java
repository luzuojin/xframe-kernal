package dev.xframe.action;

public abstract class CallableAction extends Action {

    private final Action callable;
    
    public CallableAction(ActionLoop loop, Action callable) {
        super(loop);
        this.callable = callable;
    }
    
    @Override
    public void done() {
        if(callable != null) callable.checkin();
    }
    
    public static final CallableAction of(ActionLoop loop, Runnable runnable, Action callable){
        return new CallableAction(loop, callable) {protected void exec() {runnable.run();}};
    }

}

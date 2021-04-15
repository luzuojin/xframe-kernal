package dev.xframe.task;

public class TaskChain {
    
    private ChainTask head;
    
    public TaskChain append(Task task) {
        ChainTask ct = new ChainTask(task);
        if(this.head == null) {
            this.head = ct;
        } else {
            ChainTask next = this.head;
            while(next.next != null) {
                next = next.next;
            }
            next.next = ct;
        }
        return this;
    }
    
    public void checkin() {
        if(head != null) {
            head.checkin();
        }
    }
    
    public static final TaskChain of(Task... tasks) {
        TaskChain tc = new TaskChain();
        for (Task task : tasks) {
            tc.append(task);
        }
        return tc;
    }
    
    private class ChainTask extends Task {
        private final Task task;
        private ChainTask next;
        public ChainTask(Task task) {
            super(task.loop);
            this.task = task;
        }
        @Override
        public void done() {
            if(next != null) next.checkin();
        }
        @Override
        public void exec() {
            task.exec();
        }
    }
    
}

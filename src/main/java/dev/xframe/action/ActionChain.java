package dev.xframe.action;

public class ActionChain {
    
    private ChainAction head;
    
    public ActionChain append(Action action) {
        ChainAction ca = new ChainAction(action);
        if(this.head == null) {
            this.head = ca;
        } else {
            ChainAction next = this.head;
            while(next.next != null) {
                next = next.next;
            }
            next.next = ca;
        }
        return this;
    }
    
    public void checkin() {
        if(head != null) {
            head.checkin();
        }
    }
    
    public static final ActionChain of(Action... actions) {
        ActionChain ac = new ActionChain();
        for (Action action : actions) {
            ac.append(action);
        }
        return ac;
    }
    
    private class ChainAction extends Action {
        private final Action action;
        private ChainAction next;
        public ChainAction(Action action) {
            super(action.queue);
            this.action = action;
        }
        @Override
        public void done() {
            if(next != null) next.checkin();
        }
        @Override
        public void exec() {
            action.exec();
        }
    }
    
}

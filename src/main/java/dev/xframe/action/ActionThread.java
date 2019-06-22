package dev.xframe.action;

class ActionThread extends Thread {
	
	ActionQueue attach;
	
	public ActionThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}
	
	public ActionQueue getAttach() {
		return attach;
	}
	
	public void setAttach(ActionQueue attach) {
		this.attach = attach;
	}
	
	public void unsetAttach() {
		this.attach = null;
	}

}

package dev.xframe.action;

class ActionThread extends Thread {
	
	ActionLoop attach;
	
	public ActionThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}
	
	public ActionLoop getAttach() {
		return attach;
	}
	
	public void setAttach(ActionLoop attach) {
		this.attach = attach;
	}
	
	public void unsetAttach() {
		this.attach = null;
	}

}

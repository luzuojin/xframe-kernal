package dev.xframe.metric;

public interface Scriber {
	
	Scriber NIL = new Scriber() {};

	default void onExecSlow(Guage guage) {}
	
	default void onWaitLong(Guage guage) {}

}

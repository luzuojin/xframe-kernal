package dev.xframe.metric;

public interface Scriber {
	
	Scriber NIL = new Scriber() {};

	default void onExecSlow(Gauge guage) {}
	
	default void onWaitLong(Gauge guage) {}

}

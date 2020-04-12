package dev.xframe.boot;

import dev.xframe.inject.Synthetic;

@Synthetic
public interface ShutdownAgent {
	
	public void shutdown();

}

package dev.xframe.boot;

import dev.xframe.inject.Composite;

@Composite
public interface ShutdownAgent {
	
	public void shutdown();

}

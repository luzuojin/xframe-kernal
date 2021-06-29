package dev.xframe.net.client;

import dev.xframe.inject.Composite;
import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.Session;

@Composite
public interface ClientLifecycleListener extends LifecycleListener {
	
	default void onSessionIdle(Session session) {}

}

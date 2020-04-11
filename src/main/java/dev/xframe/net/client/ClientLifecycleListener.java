package dev.xframe.net.client;

import dev.xframe.inject.Synthetic;
import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.Session;

@Synthetic
public interface ClientLifecycleListener extends LifecycleListener {
	
	public void onSessionIdle(Session session);

}

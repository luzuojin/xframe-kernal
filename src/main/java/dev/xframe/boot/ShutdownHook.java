package dev.xframe.boot;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.xframe.inject.Configurator;
import dev.xframe.inject.Eventual;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.inject.code.CompositeBuilder;
import dev.xframe.utils.XLogger;

@Configurator
public class ShutdownHook implements Eventual {

	@Inject
	private ShutdownAgent agents;
	
	private Thread hook = new Thread(this::shutdownNow, "hook");
	
	private AtomicBoolean shuttedDown = new AtomicBoolean(false);
	
	public void append(ShutdownAgent agent) {
	    BeanHelper.appendComposite(agents, agent);
	}
	
	public void shutdown() {
		shutdonw(true);
	}
	
	private void shutdonw(boolean graceful) {
		if(shuttedDown.compareAndSet(false, true)) {
			if(graceful) {
				XLogger.info("Shutting down server gracefully");
			} else {
				XLogger.info("Shutting down server immediately");
			}
			
			CompositeBuilder.forEach(agents, (ShutdownAgent agent)->{
				try {
					agent.shutdown();
					XLogger.info("Shutting down agent [{}]", agent.getClass().getSimpleName());
				} catch (Throwable e) {
					XLogger.warn("Shutting down agent [{}]", agent.getClass().getSimpleName(), e);
				}
			});
			
			try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (IllegalStateException e) {
                // ignore -- IllegalStateException means the VM is already shutting down
            }
			
			XLogger.info("Shutting down server completed");
		}
	}
	
	private void shutdownNow() {
		shutdonw(false);
	}

	@Override
	public void eventuate() {
		Runtime.getRuntime().addShutdownHook(this.hook);
	}

}

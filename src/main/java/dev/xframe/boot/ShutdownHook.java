package dev.xframe.boot;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Eventual;
import dev.xframe.inject.Ordered;
import dev.xframe.inject.Ordered.Collection;
import dev.xframe.inject.code.Codes;
import dev.xframe.utils.XLogger;

@Configurator
public class ShutdownHook implements Eventual {

	private Collection<ShutdownAgent> agents;
	
	private Thread hook = new Thread(this::shutdownNow, "hook");
	
	private AtomicBoolean shuttedDown = new AtomicBoolean(false);
	
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
			
			if(Bootstrap.RUNNING_INSTANCE != null) {
				Bootstrap.RUNNING_INSTANCE.shutdown();
				XLogger.info("Shutting down net servers");
			}
			
			for (ShutdownAgent agent : agents) {
				try {
					agent.shutdown();
					XLogger.info("Shutting down agent [" + agent.getClass().getSimpleName() + "]");
				} catch (Throwable e) {
					XLogger.warn("Shutting down agent", e);
				}
			}
			
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
		agents = Codes.getDeclaredClasses().stream()
					.filter(this::isAgent)
					.filter(this::existsBean)
					.map(this::getAgentBean)
					.collect(Collectors.toCollection(Ordered.Collection::new));
		
		Runtime.getRuntime().addShutdownHook(this.hook);
	}

	private boolean isAgent(Class<?> c) {
		return ShutdownAgent.class.isAssignableFrom(c) && !ShutdownAgent.class.equals(c);
	}

	private boolean existsBean(Class<?> c) {
		return getAgentBean(c) != null;
	}

	private ShutdownAgent getAgentBean(Class<?> c) {
		return (ShutdownAgent) ApplicationContext.fetchBean(c);
	}

}

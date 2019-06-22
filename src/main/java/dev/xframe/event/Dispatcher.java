package dev.xframe.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.action.Action;
import dev.xframe.action.ActionQueue;

public interface Dispatcher {
	
	Logger logger = LoggerFactory.getLogger(Subscriber.class);
	
	public void dispatch(Iterable<Subscriber> subscribers, Object evt);
	
	//dispatch with exception catch
	public static void doDispatch(Iterable<Subscriber> subscribers, Object evt) {
		for (Subscriber subscriber : subscribers) {
			try {
				subscriber.onEvent(evt);
			} catch (Throwable t) {
				logger.warn("Event subscriber throws ", t);
			}
		}
    }
	
	public static Dispatcher queued(ActionQueue queue) {
		return new QueuedDispatcher(queue);
	}
	
	public static Dispatcher direct() {
		return new DirectDispatcher();
	}
	
	static class DirectDispatcher implements Dispatcher {
		@Override
		public void dispatch(Iterable<Subscriber> subscribers, Object evt) {
			Dispatcher.doDispatch(subscribers, evt);
		}
	}
	
	static class QueuedDispatcher implements Dispatcher {
		final ActionQueue queue;
		public QueuedDispatcher(ActionQueue queue) {
			this.queue = queue;
		}
		@Override
		public void dispatch(Iterable<Subscriber> subscribers, Object evt) {
			if(ActionQueue.getCurrent() == queue) {
				Dispatcher.doDispatch(subscribers, evt);
			} else {
				new Action(queue) {
					protected void exec() {
						Dispatcher.doDispatch(subscribers, evt);
					}
				}.checkin();
			}
		}
	}

}

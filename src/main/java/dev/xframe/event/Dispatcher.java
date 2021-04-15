package dev.xframe.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.task.Task;
import dev.xframe.task.TaskLoop;

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
	
	public static Dispatcher looped(TaskLoop loop) {
		return new LoopedDispatcher(loop);
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
	
	static class LoopedDispatcher implements Dispatcher {
		final TaskLoop loop;
		public LoopedDispatcher(TaskLoop loop) {
			this.loop = loop;
		}
		@Override
		public void dispatch(Iterable<Subscriber> subscribers, Object evt) {
			if(loop.inLoop()) {
				Dispatcher.doDispatch(subscribers, evt);
			} else {
				new Task(loop) {
					protected void exec() {
						Dispatcher.doDispatch(subscribers, evt);
					}
				}.checkin();
			}
		}
	}

}

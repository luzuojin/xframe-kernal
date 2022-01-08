package dev.xframe.event;

import dev.xframe.utils.XGeneric;

import java.util.function.Consumer;

public abstract class Subscriber {
    
    protected final int group;
    protected final int type;
    
    public Subscriber(int group, int type) {
        this.group = group;
        this.type = type;
    }
	public int group() {
		return group;
	}
	public int type() {
		return type;
	}

	public abstract void onEvent(Object event) throws Exception;
    
    public static <X> Subscriber of(int group, Consumer<X> sub) {
    	Class<?> evtType = XGeneric.parse(sub.getClass(), Consumer.class).getOnlyType();
    	return new SimpleSubscriber<X>(group, evtType.getAnnotation(Event.class).value(), sub);
    }
    
    static final class SimpleSubscriber<X> extends Subscriber {
    	final Consumer<X> sub;
		SimpleSubscriber(int group, int type, Consumer<X> sub) {
			super(group, type);
			this.sub = sub;
		}
	    @SuppressWarnings("unchecked")
		public void onEvent(Object event) {
			sub.accept((X) event);
		}
	}

}

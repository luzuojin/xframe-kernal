package dev.xframe.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import dev.xframe.event.Event;
import dev.xframe.event.EventBus;
import dev.xframe.event.Registry;
import dev.xframe.event.Subscribe;

public class EventsTest {
    
	@Event(1)
	static class T1Event {
		final int code = 1;
	}
	@Event(2)
	static class T2Event {
		final int code = 2;
	}
	
    AtomicInteger flag = new AtomicInteger(0);
    
    @Subscribe
    public void apply(T2Event evt) {
    	flag.addAndGet(evt.code);
    }
    
    @Test
    public void test() {
        EventBus eventBus = new EventBus();
        Registry.regist(this, eventBus);
        eventBus.regist((T1Event e)->flag.addAndGet(e.code));
        
        eventBus.post(new T1Event());
        Assert.assertEquals(flag.get(), 1);
        
        eventBus.post(new T2Event());
        Assert.assertEquals(flag.get(), 3);
        
        Registry.unregist(EventsTest.class, eventBus);
        
        eventBus.post(new T1Event());
        Assert.assertEquals(flag.get(), 4);
        
        eventBus.post(new T2Event());//unregisted
        Assert.assertEquals(flag.get(), 4);
    }

}

package dev.xframe.event;

import java.util.Iterator;
import java.util.List;

import dev.xframe.utils.SyncCOWList;

public class EventBus extends SubscriberMap implements Registrator {
    
    public static int getEventType(Object evt) {
        return evt.getClass().getAnnotation(Event.class).value();
    }
	
	private Dispatcher dispatcher;
	
	public EventBus() {
		this(Dispatcher.direct());
	}
	public EventBus(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
    public void post(Object evt) {
        List<Subscriber> list = get(getEventType(evt));
        if(list != null && !list.isEmpty()) {
        	dispatcher.dispatch(list, evt);
        }
    }

	@Override
    public void regist(Subscriber subscriber) {
        getOrNew(subscriber.type).add(subscriber);
    }

	@Override
    public void unregist(int group) {
		delByGroup(group);
    }
	
	public void unregist(Subscriber subscriber) {
		del(subscriber);
	}

}

class SubscriberMap {//减少内存占用(相关Ref/Wrapper都去掉)
    static class Entry extends SyncCOWList<Subscriber> {
        int type;
        Entry next;
        public Entry(int type) {
            this.type = type;
        }
    }
    private int count;
    private Entry[] datas;
    public SubscriberMap() {
        this.datas = new Entry[16];
    }
    
    protected List<Subscriber> getOrNew(int type) {
        List<Subscriber> list = get(type);
        return list == null ? getOrNewSafely(type) : list;
    }

    private synchronized List<Subscriber> getOrNewSafely(int type) {//只有new Entry时才会有并发问题 (其他时机的并发由cowlist处理)
        Entry list = get(type);
        if(list == null) {
            list = new Entry(type);
            put(type, list);
        }
        return list;
    }
    
    private int indexOf(int key) {
        return key & (datas.length - 1);
    }
    
    protected Entry get(int key) {
        Entry node = datas[indexOf(key)];
        while(node != null) {
            if(node.type == key) {
                return node;
            }
            node = node.next;
        }
        return null;
    }
    
    private void put(int key, Entry val) {
        put0(key, val);
        
        if(count > datas.length) {
            resize();
        }
    }

    private void put0(int key, Entry val) {
        int i = indexOf(key);
        Entry data = datas[i];
        if(data == null) {
            datas[i] = val;
        } else {
            while(data.next != null) {
                data = data.next;
            }
            data.next = val;
        }
        ++ count;
    }

    private void resize() {
        Entry[] old = increase();
        for(Entry s : old) {
            while(s != null) {
                Entry t = s.next;
                s.next = null;//make pure
                put0(s.type, s);
                s = t;
            }
        }
    }

    private Entry[] increase() {
        Entry[] old = datas;
        datas = new Entry[(old.length << 1)];
        count = 0;
        return old;
    }
    
    protected void delByGroup(int group) {
        for(Entry s : datas) {
            while(s != null) {
                Iterator<Subscriber> it = s.iterator();
                while(it.hasNext()) {
                    Subscriber next = it.next();
                    if(next.group == group) {
                        s.remove(next);
                    }
                }
                s = s.next;
            }
        }
    }
    
    protected void del(Subscriber subscriber) {
        Entry datas = get(subscriber.type);
        if(datas != null && !datas.isEmpty()) {
            datas.remove(subscriber);
        }
    }
}

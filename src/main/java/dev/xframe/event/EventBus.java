package dev.xframe.event;

import java.util.Iterator;
import java.util.List;

import dev.xframe.utils.SyncCOWList;

public class EventBus implements Registrator {
	
	static class SubList extends SyncCOWList<Subscriber> {
		int type;
		SubList next;
		public SubList(int type) {
			this.type = type;
		}
	}
	
	private int subListCount;
	private SubList[] subLists;
	private Dispatcher dispatcher;
	
	public EventBus() {
		this(Dispatcher.direct());
	}
	public EventBus(Dispatcher dispatcher) {
		this.subLists = new SubList[16];
		this.dispatcher = dispatcher;
	}
	
    public void post(Object evt) {
        List<Subscriber> list = get0(getEventType(evt));
        if(list != null && !list.isEmpty()) {
        	dispatcher.dispatch(list, evt);
        }
    }

    static int getEventType(Object evt) {
		return evt.getClass().getAnnotation(Event.class).value();
	}

	@Override
    public void regist(Subscriber subscriber) {
        getOrNew(subscriber.type).add(subscriber);
    }

	@Override
    public void unregist(int group) {
		delSubsByType(group);
    }
	
	public void unregist(Subscriber subscriber) {
		delSub(subscriber);
	}
	
	
	private List<Subscriber> getOrNew(int type) {
        List<Subscriber> list = get0(type);
        return list == null ? getOrNewSafely(type) : list;
    }

    private synchronized List<Subscriber> getOrNewSafely(int type) {//只有new sublist时才会有并发问题 (其他时机的并发由cowlist处理)
        SubList list = get0(type);
        if(list == null) {
            list = new SubList(type);
            put(type, list);
        }
        return list;
    }
    
    private int indexOf(int key) {
        return key & (subLists.length - 1);
    }
    
    private SubList get0(int key) {
        SubList node = subLists[indexOf(key)];
        while(node != null) {
            if(node.type == key) {
                return node;
            }
            node = node.next;
        }
        return null;
    }
    
    private void put(int key, SubList val) {
        put0(key, val);
        
        if(subListCount > subLists.length) {
            resize();
        }
    }

    private void put0(int key, SubList val) {
        int i = indexOf(key);
        SubList subList = subLists[i];
        if(subList == null) {
            subLists[i] = val;
        } else {
            while(subList.next != null) {
                subList = subList.next;
            }
            subList.next = val;
        }
        ++ subListCount;
    }

    private void resize() {
        SubList[] old = increase();
        for(SubList s : old) {
            while(s != null) {
                SubList t = s.next;
                s.next = null;//make pure
                put0(s.type, s);
                s = t;
            }
        }
    }

    private SubList[] increase() {
        SubList[] old = subLists;
        subLists = new SubList[(old.length << 1)];
        subListCount = 0;
        return old;
    }
	
	private void delSubsByType(int group) {
        for(SubList s : subLists) {
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
	
    private void delSub(Subscriber subscriber) {
        SubList subList = get0(subscriber.type);
		if(subList != null && !subList.isEmpty()) {
			subList.remove(subscriber);
		}
    }

}

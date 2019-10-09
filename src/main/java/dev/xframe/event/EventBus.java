package dev.xframe.event;

import java.util.Iterator;
import java.util.List;

import dev.xframe.utils.SyncCOWList;

public class EventBus implements Registrator {
	
	static class Node extends SyncCOWList<Subscriber> {
		final int type;
		Node next;
		public Node(int type) {
			this.type = type;
		}
	}
	
	int count;
	Node[] nodes;
	Dispatcher disper;
	
	public EventBus() {
		this(Dispatcher.direct());
	}
	public EventBus(Dispatcher dispatcher) {
		this.nodes = new Node[16];
		this.disper = dispatcher;
	}
	
	private int indexOf(int key) {
		return key & (nodes.length - 1);
	}
	
	private Node get(int key) {
		Node node = nodes[indexOf(key)];
		while(node != null) {
			if(node.type == key) {
				return node;
			}
			node = node.next;
		}
		return null;
	}
	
	private void put(int key, Node val) {
		put0(key, val);
		
		if(count > nodes.length) {
			resize();
		}
	}

	private void put0(int key, Node val) {
		int i = indexOf(key);
		Node node = nodes[i];
		if(node == null) {
			nodes[i] = val;
		} else {
			while(node.next != null) {
				node = node.next;
			}
			node.next = val;
		}
		++ count;
	}

    private void resize() {
    	Node[] old = increase();
    	for(Node n : old) {
			while(n != null) {
				Node t = n.next;
				n.next = null;//make pure
				put0(n.type, n);
				n = t;
			}
		}
	}

	private Node[] increase() {
		Node[] old = nodes;
		nodes = new Node[(old.length << 1)];
    	count = 0;
    	return old;
	}

    public void post(Object evt) {
        List<Subscriber> list = get(getEventType(evt));
        if(list != null && !list.isEmpty()) {
        	disper.dispatch(list, evt);
        }
    }

    static int getEventType(Object evt) {
		return evt.getClass().getAnnotation(Event.class).value();
	}

	@Override
    public void regist(Subscriber subscriber) {
        getOrNew(subscriber.type).add(subscriber);
    }

    private List<Subscriber> getOrNew(int type) {
        List<Subscriber> list = get(type);
        return list == null ? getOrNewSafely(type) : list;
    }

    private synchronized List<Subscriber> getOrNewSafely(int type) {
        Node list = get(type);
        if(list == null) {
            list = new Node(type);
            put(type, list);
        }
        return list;
    }

	@Override
    public void unregist(int group) {
		for(Node n : nodes) {
			while(n != null) {
				Iterator<Subscriber> it = n.iterator();
				while(it.hasNext()) {
	                Subscriber next = it.next();
	                if(next.group == group) {
	                    n.remove(next);
	                }
	            }
				n = n.next;
			}
		}
    }
	
	public void unregist(Subscriber subscriber) {
		Node node = get(subscriber.type);
		if(node != null && !node.isEmpty()) {
			node.remove(subscriber);
		}
	}

}

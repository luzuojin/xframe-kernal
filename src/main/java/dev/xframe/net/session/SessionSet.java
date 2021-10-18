package dev.xframe.net.session;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class SessionSet implements Iterable<Session> {
    
    private final Object lock = new Object();
    
    private Session[] sessions;
    private AtomicInteger idx;
    private int size;
    
    public SessionSet() {
        this(0);
    }
    public SessionSet(int capacity) {
        this.sessions = new Session[capacity];
        this.idx = new AtomicInteger(-1);
        this.size = 0;
    }

    private void incrCapacity() {
        if(size < sessions.length)
            return;
        sessions = Arrays.copyOf(sessions, sessions.length + 1);
    }
    
    public void add(Session session) {
        synchronized (lock) {
            incrCapacity();
            
            for (int i = 0; i < sessions.length; i++) {
                if(sessions[i] == null) {
                    sessions[i] = session;
                    ++ size;
                    break;
                }
            }
        }
    }
    
    public Session get(int index) {
        if(index < 0 || index >= sessions.length) {
            return null;
        }
        return sessions[index];
    }
    
    public Session next() {
        return get(nextIndex());
    }
    
    private int nextIndex0() {
        for (;;) {
            int current = idx.get();
            int next = current + 1;
            if(next >= size) {
                next = 0;
            }
            if (idx.compareAndSet(current, next)) {
                return next;
            }
        }
    }
    
    public int nextIndex() {
        int len = sessions.length;
        for (int i = 0; i < len; i++) { // 只找一圈
            int pos = nextIndex0();
            if (sessions[pos] != null) {// not empty pos
                return pos;
            }
        }
        return -1;
    }
    
    public Session remove(Session session) {
        for (int i = 0; i < sessions.length; i++) {
            if(sessions[i] == session) {
                return remove(i);
            }
        }
        return null;
    }
    
    private Session remove(int idx) {
        synchronized (lock) {
            return remove0(idx);
        }
    }

    private Session remove0(int idx) {
        Session re = sessions[idx];
        sessions[idx] = null;
        if(re != null) {
            --this.size;
            re.close();
        }
        return re;
    }
    
    public void close() {
        synchronized (lock) {
            for (int i = 0; i < sessions.length; i++) {
                remove0(i);
            }
        }
    }
    
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<Session> iterator() {
        return new SSIterator();
    }
    
    public class SSIterator implements Iterator<Session> {
        private int itIdx;
        private int curIdx;
        
        @Override
        public boolean hasNext() {
            while(itIdx < sessions.length) {
                if(sessions[itIdx] != null) return true;
                ++ itIdx;
            }
            return false;
        }

        @Override
        public Session next() {
            Session ret = sessions[itIdx];
            curIdx = itIdx;
            ++ itIdx;
            return ret;
        }

        @Override
        public void remove() {
            SessionSet.this.remove(curIdx);
        }
    }

}

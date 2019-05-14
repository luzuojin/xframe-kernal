package dev.xframe.net.session;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class SessionSet implements Iterable<Session> {
    
    private Session[] sessions;
    private int idx;
    private int size;
    private ReentrantLock lock;
    
    public SessionSet() {
        this(0);
    }
    public SessionSet(int capacity) {
        this.sessions = new Session[capacity];
        this.idx = -1;
        this.size = 0;
        this.lock = new ReentrantLock();
    }

    private void incrCapacity() {
        if(size < sessions.length) return;
        
        sessions = Arrays.copyOf(sessions, sessions.length + 1);
    }
    
    public void add(Session session) {
        lock.lock();
        try {
            incrCapacity();
            
            for (int i = 0; i < sessions.length; i++) {
                if(sessions[i] == null) {
                    sessions[i] = session;
                    ++ size;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    public Session get() {
        if(this.size == 0) return null;
        
        lock.lock();
        try {
            if(this.size == 0) return null;//double check
            
            int len = this.sessions.length;
            int next = this.idx;
            for(int i=0; i<len; i++) {//只找一圈
                ++ next;
                if(next >= len) next = 0;
                
                Session ret = sessions[next];
                if(ret != null) {
                    this.idx = next;
                    return ret;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            return remove0(idx);
        } finally {
            lock.unlock();
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
        lock.lock();
        try {
            for (int i = 0; i < sessions.length; i++) {
                remove0(i);
            }
        } finally {
            lock.unlock();
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

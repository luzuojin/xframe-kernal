package dev.xframe.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * fast attachment get and set
 * 
 * @author luzj
 *
 */
public class XAttachment {
    
    private final AtomicInteger idx = new AtomicInteger(0);
    
    private volatile boolean freezon = false;
    
    public AttachKey newKey() {
        if(freezon) {
            throw new IllegalStateException("Attachment key must instanced before holder");
        }
        if(idx.get() > 16) {
            throw new IllegalStateException("Too many attachment keys.");
        }
        return new AttachKey(idx.getAndIncrement());
    }
    
    public AttachHolder newHolder() {
        if(!freezon) {
            freezon = true;
        }
        return new AttachHolder(idx.get());
    }
    
    public static class AttachKey {
        private final int index;
        private AttachKey(int index) {
            this.index = index;
        }
    }
    
    public static class AttachHolder {
        private Object[] values;
        private AttachHolder(int len) {
            this.values = new Object[len];
        }
        public void set(AttachKey key, Object val) {
            this.values[key.index] = val;
        }
        @SuppressWarnings("unchecked")
        public <T> T get(AttachKey key) {
            return (T) values[key.index];
        }
    }

}

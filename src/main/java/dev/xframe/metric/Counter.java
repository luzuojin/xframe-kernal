package dev.xframe.metric;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 计数器
 * @author luzj
 */
@SuppressWarnings("serial")
public class Counter extends AtomicLong {

    /**
     * 与value比较取最大值为value设置
     * @param val
     */
    public void max(long val) {
        for(;;) {
            long cur = get();
            if(val < cur) 
                break;
            if(compareAndSet(cur, val))
                break;
        }
    }
    
    public void incr() {
        super.getAndIncrement();
    }
    
    public void add(long val) {
        super.getAndAdd(val);
    }
    
}

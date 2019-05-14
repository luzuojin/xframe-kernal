package dev.xframe.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metric {
    
    static final Logger logger = LoggerFactory.getLogger(Metric.class);
    
    static boolean watching = false;//默认不开启
    
    final Class<?> ident;
    
    final AtomicLong cnt = new AtomicLong();//执行次数
    final AtomicLong sum = new AtomicLong();//执行总时间
    final AtomicLong max = new AtomicLong();//最长执行时间
    final AtomicLong wat = new AtomicLong();//最长等待时间
    
    public Metric(Class<?> ident) {
        this.ident = ident;
    }
    
    static final Map<Class<?>, Metric> metrics = new HashMap<>();
    
    static Metric get(Class<?> ident) {
        Metric metric = metrics.get(ident);
        if(metric == null) {
            synchronized (metrics) {
                metric = metrics.get(ident);
                if(metric == null) {
                    metric = new Metric(ident);
                    metrics.put(ident, metric);
                }
            }
        }
        return metric;
    }
    
    static void updateMax(AtomicLong s, long max) {
        for(;;) {
            long current = s.get();
            if(max < current) 
                break;
            if(s.compareAndSet(current, max))
                break;
        }
    }
    
    @Override
    public String toString() {
        return "" +
               cnt.get() + '\t' +
               sum.get() + '\t' +
               sum.get() / cnt.get() + '\t' +
               max.get() + '\t' +
               wat.get() + '\t' +
               ident.getName();
    }
    
    public static Map<Class<?>, Metric> metrics() {
        return metrics;
    }
    
    public static String columns() {
        return  "count\t" + 
                "sum  \t" +
                "avg  \t" +
                "max  \t" +
                "wait \t" +
                "ident\t";
    }
    
    public static void gauge(Class<?> ident, long create, long start, long end, Metrical metrical) {
        if(watching) {
            long used = end - start;
            long wait = start - create;
            
            gauge(ident, used, wait);
            
            if (used > 1000 || wait > 6000) {
                logger.warn("Execute slow [" + ident.getName() + "] used: " + used + ", wait: " + wait + ", size: " + metrical.waitings());
            }
        }
    }
    
    public static void gauge(Class<?> ident, long create, long start, long end) {
        if(watching) {
            gauge(ident, end - start, start - create);
        }
    }

    private static void gauge(Class<?> ident, long used, long wait) {
        try {
            Metric metric = get(ident);
            
            metric.cnt.incrementAndGet();
            metric.sum.addAndGet(used);
            
            updateMax(metric.max, used);
            updateMax(metric.wat, wait);
        } catch (Throwable e) {
            //ignore
        }
    }
    
    public static void watch() {
        watching = true;
    }

}

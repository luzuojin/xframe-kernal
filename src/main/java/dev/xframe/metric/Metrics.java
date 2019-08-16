package dev.xframe.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.action.Action;

public class Metrics {
    
    static final Logger logger = LoggerFactory.getLogger(Metrics.class);
    
    static boolean watching = false;//默认不开启
    
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
    
    public static Map<Class<?>, Metric> metrics() {
        return metrics;
    }
    
    public static String columns() {
        return Metric.columns();
    }
    
    public static void gauge(Class<?> ident, long create, long start, long end, Action action) {
        if(watching) {
        	try {
        		long used = end - start;
        		long wait = start - create;

        		Metric metric = get(ident);
        		gauge(metric, used, wait);

        		if (used > 1000) {
        			logger.warn("Execute slow [" + ident.getName() + "] used: " + used + ", wait: " + wait + ", size: " + action.loopings());
        			metric.slo.incrementAndGet();
        		} else if (wait > 6000) {
        			logger.warn("Execute slow [" + ident.getName() + "] used: " + used + ", wait: " + wait + ", size: " + action.loopings());
        		}
        	 } catch (Throwable e) {
                 //ignore
             }
        }
    }
    
    private static void gauge(Metric metric, long used, long wait) {
    	metric.cnt.incrementAndGet();
    	metric.sum.addAndGet(used);

    	updateMax(metric.max, used);
    	updateMax(metric.wat, wait);
    }
    
    public static void watch() {
        watching = true;
    }

}

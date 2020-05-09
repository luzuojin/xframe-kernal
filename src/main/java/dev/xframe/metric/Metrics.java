package dev.xframe.metric;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
    
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
    
    public static Map<Class<?>, Metric> metrics() {
        return metrics;
    }
    
    public static String columns() {
        return Metric.columns();
    }
    
    public static void watch() {
        watching = true;
    }
    
    public static void unwatch() {
    	watching = false;
    }

	public static void apply(Gauge g) {
		if(watching) {
        	try {
        		get(g.ident).apply(g);
        	 } catch (Throwable e) {
                 //ignore
             }
        }
	}

}

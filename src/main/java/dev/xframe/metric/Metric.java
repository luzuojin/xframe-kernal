package dev.xframe.metric;

import java.util.HashMap;
import java.util.Map;

public class Metric {
    
    static int wait_threshold = 3000;//ms
    static int slow_threshold = 500;
    
    static boolean watching = false;//默认不开启
    
    static final Map<Class<?>, Meter> meters = new HashMap<>();
    
    static Meter get(Class<?> ident) {
        Meter meter = meters.get(ident);
        if(meter == null) {
            meter = getOrNew(ident);
        }
        return meter;
    }
    static synchronized Meter getOrNew(Class<?> ident) {
        Meter meter = meters.get(ident);
        if(meter == null) {
            meter = new Meter(ident);
            meters.put(ident, meter);
        }
        return meter;
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

    public static String console() {
        return Reporter.console(meters.values());
    }

}

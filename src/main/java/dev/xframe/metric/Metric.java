package dev.xframe.metric;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.xframe.utils.XProperties;

public abstract class Metric {
    
    static final int wait_threshold = XProperties.getAsInt("xframe.metric.waitthreshold", 3000);//ms
    static final int slow_threshold = XProperties.getAsInt("xframe.metric.slowthreshold", 500);
    
    static Metric metric = XProperties.getAsBool("xframe.metric.open", true) ? new Memory() : new Empty();
    
    abstract void apply0(Gauge g);
    abstract Collection<Meter> meters();
    abstract Gauge gauge0(String ident);
    
    static class Empty extends Metric {
		void apply0(Gauge g) {
			//do nothing
		}
		Collection<Meter> meters() {
			return Collections.emptyList();
		}
		Gauge gauge0(String ident) {
			return Gauge.NIL;
		}
    }
    
    static class Memory extends Metric {
		final Map<String, Meter> meters = new HashMap<>();
	    Meter get(String ident) {
	        Meter meter = meters.get(ident);
	        if(meter == null)
	        	meter = getOrNew(ident);
	        return meter;
	    }
	    synchronized Meter getOrNew(String ident) {
	        Meter meter = meters.get(ident);
	        if(meter == null) {
	            meter = new Meter(ident);
	            meters.put(ident, meter);
	        }
	        return meter;
	    }
		void apply0(Gauge g) {
	    	get(g.ident).apply(g);
		}
		Collection<Meter> meters() {
			return meters.values();
		}
		Gauge gauge0(String ident) {
			return new Gauge(ident);
		}
    }
    
    public static void open() {
        if(metric instanceof Empty) {
        	metric = new Memory();
        }
    }
    
    public static void close() {
    	if(!(metric instanceof Empty)) {
    		metric = new Empty();
    	}
    }
    
    public static void clean() {
    	metric = new Memory();
    }
    
    public static Gauge gauge(String ident) {
		return metric.gauge0(ident);
	}
	
	public static void apply(Gauge g) {
		try {
			metric.apply0(g);
		} catch (Throwable e) {
			//ignore
		}
	}

    public static String console() {
        return Reporter.console(metric.meters());
    }

}

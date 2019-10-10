package dev.xframe.metric;

import java.util.concurrent.atomic.AtomicLong;

public class Metric {
	
	private final Class<?> ident;
    
    private final AtomicLong cnt = new AtomicLong();//执行次数
    private final AtomicLong sum = new AtomicLong();//执行总时间
    private final AtomicLong max = new AtomicLong();//最长执行时间
    private final AtomicLong wat = new AtomicLong();//最长等待时间
    private final AtomicLong slo = new AtomicLong();//slow count
    
    public Metric(Class<?> ident) {
        this.ident = ident;
    }
    
    @Override
    public String toString() {
        return "" +
               cnt.get() + '\t' +
               sum.get() + '\t' +
               sum.get() / cnt.get() + '\t' +
               max.get() + '\t' +
               wat.get() + '\t' +
               wat.get() + '\t' +
               ident.getName();
    }
    
    public static String columns() {
        return  "count\t" + 
                "sum  \t" +
                "avg  \t" +
                "max  \t" +
                "wait \t" +
                "slow \t" +
                "ident\t";
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
    
    void apply(long used, long wait) {
    	cnt.incrementAndGet();
    	sum.addAndGet(used);

    	updateMax(max, used);
    	updateMax(wat, wait);
    }
    
	public void apply(Guage g) {
		long used = g.used();
		long waited = g.waited();
		
		apply(used, waited);

		if (used > 1000) {
			g.scriber.onExecSlow(g);
			slo.incrementAndGet();
		} else if (waited > 6000) {
			g.scriber.onWaitLong(g);
		}
	}

}

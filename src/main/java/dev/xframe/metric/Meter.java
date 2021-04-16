package dev.xframe.metric;

import static dev.xframe.metric.Metric.slow_threshold;
import static dev.xframe.metric.Metric.wait_threshold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 时间段内的请求次数
 * @author luzj
 *
 */
public class Meter {

    static final Logger logger = LoggerFactory.getLogger(Metric.class);
    
    final String ident;
    
    final Counter cnt = new Counter();//执行次数
    final Counter sum = new Counter();//执行总时间
    final Counter max = new Counter();//最长执行时间
    final Counter wat = new Counter();//最长等待时间
    final Counter slo = new Counter();//slow count
    
    public Meter(String ident) {
        this.ident = ident;
    }
    
    void apply(long used, long wait) {
        cnt.incr();
        sum.add(used);

        max.max(used);
        wat.max(wait);
    }
    
    public void apply(Gauge g) {
        long used = g.used();
        long waited = g.waited();
        
        apply(used, waited);

        if (used > slow_threshold) {
            logSlow(g);
            slo.incr();
        } else if (waited > wait_threshold) {
            logSlow(g);
        }
    }
    
    public static void logSlow(Gauge g) {
        logger.warn("Execute slow [" + g.ident() + "] used: " + g.used() + ", waited: " + g.waited());
    }

}

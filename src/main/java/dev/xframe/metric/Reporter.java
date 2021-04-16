package dev.xframe.metric;

import java.util.Collection;

/**
 * 显示jmx/console
 * @author luzj
 *
 */
public class Reporter {
    
    public static String console(Collection<Meter> meters) {
        StringBuilder sb = new StringBuilder();
        sb.append(header()).append('\n');
        for (Meter meter : meters) {
            sb.append(format(meter)).append('\n');
        }
        return sb.toString();
    }
    
    private static String format(Meter meter) {
        return "" +
               meter.cnt.get() + '\t' +
               meter.sum.get() + '\t' +
               meter.sum.get() / meter.cnt.get() + '\t' +
               meter.max.get() + '\t' +
               meter.wat.get() + '\t' +
               meter.slo.get() + '\t' +
               meter.ident;
    }

    private static String header() {
        return  "count\t" + 
                "sum  \t" +
                "avg  \t" +
                "max  \t" +
                "wait \t" +
                "slow \t" +
                "ident\t";
    }

}

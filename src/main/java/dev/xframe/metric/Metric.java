package dev.xframe.metric;

import java.util.concurrent.atomic.AtomicLong;

public class Metric {
	
	final Class<?> ident;
    
    final AtomicLong cnt = new AtomicLong();//执行次数
    final AtomicLong sum = new AtomicLong();//执行总时间
    final AtomicLong max = new AtomicLong();//最长执行时间
    final AtomicLong wat = new AtomicLong();//最长等待时间
    
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
               ident.getName();
    }
    
    public static String columns() {
        return  "count\t" + 
                "sum  \t" +
                "avg  \t" +
                "max  \t" +
                "wait \t" +
                "ident\t";
    }

}

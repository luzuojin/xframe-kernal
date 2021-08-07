package dev.xframe.task.scheduled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.util.function.LongToIntFunction;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Scheduled {

    /**
     * 第一次执行偏移量
     */
    int delay() default 0;
    
    /**
     * 周期间隔
     */
    int period() default -1;

    /**
     * period & delay 时间单位
     * @return
     */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /**
     * delay基准线, 小时/天/周(delay在基准线基础上偏移)
     * period为-1时 使用Baseline.period为默认值
     */
    Baseline baseline() default Baseline.CURRENT;
    
    public enum Baseline {
        //period为-1时不会重复执行
        CURRENT (-1, offset-> {
            return (int) offset;
        }),
        HOURLY  (TimeUnit.HOURS.toMillis(1), offset->{
            long now = System.currentTimeMillis();
            long off = offset - (now - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(now)));
            long dly = off > 0 ? off : TimeUnit.HOURS.toMillis(1) + off;//off < 0
            return (int) dly;
        }),
        DAILY   (TimeUnit.DAYS.toMillis(1), offset->{
            long now = System.currentTimeMillis();
            long off = offset - (now - TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(now)));
            long dly = off > 0 ? off : TimeUnit.DAYS.toMillis(1) + off;//off < 0
            return (int) dly;
        }),
        WEEKLY  (TimeUnit.DAYS.toMillis(7), offset->{
            long now = System.currentTimeMillis();
            long days = TimeUnit.MILLISECONDS.toDays(now);
            long daysInWeek = ((days - 3) % 7); //周日为第一天
            long baseline = TimeUnit.DAYS.toMillis(days) - TimeUnit.DAYS.toMillis(daysInWeek);
            long off = offset - (now - baseline);
            long dly = off > 0 ? off : TimeUnit.DAYS.toMillis(7) + off;//off < 0
            return (int) dly;
        });
        
        /**
         * period为-1时 使用默认period, 不需要重复执行时可设置为0
         */
        public final int period;
        /**
         * 根据偏移量计算时间差(delay), 如果当前时间已超出该同期偏移量则增加一个周期
         */
        public final LongToIntFunction delayCaculator;
        
        private Baseline(long period, LongToIntFunction offsetCaculator) {
            this.period = (int) period;
            this.delayCaculator = offsetCaculator;
        }
    }
}

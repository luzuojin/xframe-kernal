package dev.xframe.task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public class DailyTask extends Task {
    
    final int hour;
    final int minute;
    final Runnable runnable;
    
    long nextExecTime;
    
    boolean isRunning;
    
    public DailyTask(String name, int hour, int minute, long nextExecTime, Runnable runnable) {
        super("daily-" + name, 1, 1, TimeUnit.MINUTES);
        this.hour = hour;
        this.minute = minute;
        this.runnable = runnable;
        this.nextExecTime = nextExecTime;
        
        if(this.nextExecTime == 0) setNextExecTime();
    }
    
    @Override
    public void exec() {
        if (!isRunning && requireExec()) {
            isRunning = true;
            try {
                runnable.run();
            } catch (Throwable ex) {
                logger.error("Run task [" + name + "] error", ex);
            } finally {
                setNextExecTime();
                isRunning = false;
            }
        }
    }
    
    protected boolean requireExec() {
        return currentTimeMillis.getAsLong() > nextExecTime;
    }
    
    protected void setNextExecTime() {
        nextExecTime = calcDailyNextTime(hour, minute);
    }
    
    public long getNextExecTime() {
        return nextExecTime;
    }
    
    protected static LongSupplier currentTimeMillis = System::currentTimeMillis;
    public void setCurrentTimeMillis(LongSupplier currentTimeMillis) {
        DailyTask.currentTimeMillis = currentTimeMillis;
    }
    
    protected static ZoneOffset offset = ZoneOffset.ofTotalSeconds((int) TimeUnit.MILLISECONDS.toSeconds(TimeZone.getDefault().getRawOffset()));
    
    /**
     * next time when hour & minute
     */
    public static long calcDailyNextTime(int hour, int minute) {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis.getAsLong()), offset);
        LocalDateTime todyTime = now.toLocalDate().atTime(hour, minute);
        return now.isBefore(todyTime) ?
                todyTime.toInstant(offset).toEpochMilli() :
                todyTime.plusDays(1).toInstant(offset).toEpochMilli();
    }
    
    public static DailyTask of(String name, int hour, Runnable runnable) {
        return new DailyTask(name, hour, 0, 0, runnable);
    }
    public static DailyTask of(String name, int hour, int minute, Runnable runnable) {
        return new DailyTask(name, hour, minute, 0, runnable);
    }
    public static DailyTask of(String name, int hour, long nextExecTime, Runnable runnable) {
        return new DailyTask(name, hour, 0, nextExecTime, runnable);
    }
    public static DailyTask of(String name, int hour, int minute, long nextExecTime, Runnable runnable) {
        return new DailyTask(name, hour, minute, nextExecTime, runnable);
    }

}

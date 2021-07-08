package dev.xframe.task.scheduled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Scheduled {
    
	/**
	 * 周期间隔
	 */
	int period() default -1;
	
	/**
	 * 延迟多长时间第一次执行
	 */
	int delay() default -1;

	/**
	 * 每日任务, delay以每日0点(系统默认时区)为基准
	 */
	boolean daily() default false;
	
}

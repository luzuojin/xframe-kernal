package dev.xframe.action.scheduled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
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
	 * 每天执行一次的任务(距标准时间0点的偏移量)
	 */
	int dailyOffset() default -1;
	
}

package dev.xframe.boot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ShutdownOrder {
	/**
	 * 大 -->小
	 * big-->small
	 */
	public int value();

}

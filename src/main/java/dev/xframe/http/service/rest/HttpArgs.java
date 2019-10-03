package dev.xframe.http.service.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
public @interface HttpArgs {

	 @Retention(RetentionPolicy.RUNTIME)
	 @Target(ElementType.PARAMETER)    
	 public @interface Body {
		 String value() default "";//arg name
	 }
	 
	 @Retention(RetentionPolicy.RUNTIME)
	 @Target(ElementType.PARAMETER)    
	 public @interface Param {
		 String value() default "";//arg name
	 }
	 
	 @Retention(RetentionPolicy.RUNTIME)
	 @Target(ElementType.PARAMETER)    
	 public @interface Header {
		 String value() default "";//arg name
	 }
	 
	 @Retention(RetentionPolicy.RUNTIME)
	 @Target(ElementType.PARAMETER)    
	 public @interface Path {
		 String value() default "";//arg name
	 }
	
}

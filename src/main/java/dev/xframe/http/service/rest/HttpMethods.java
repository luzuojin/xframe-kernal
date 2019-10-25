package dev.xframe.http.service.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)    
public @interface HttpMethods {//use @interface for completion
	
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)    
    public @interface GET {
        String value() default "";//sub uri
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)    
    public @interface POST {
        String value() default "";//sub uri
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)    
    public @interface PUT {
        String value() default "";//sub uri
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)    
    public @interface DELETE {
        String value() default "";//sub uri
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)    
    public @interface OPTIONS {
        String value() default "";//sub uri
    }

}

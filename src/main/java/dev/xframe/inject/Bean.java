package dev.xframe.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bean {
    
    /**
     * 是否可以reload, 如果可以则表明 该类可以hotswap
     */
    public boolean reloadable() default false;
    
}

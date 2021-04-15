package dev.xframe.game;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Assemble {

    /**
     * 默认availableProcessors
     */
    int threads() default 0;//logics threads
    
    /**
     * 是否使用ShardingTaskExecutor
     */
    boolean sharding() default true;
    
}

package dev.xframe.inject.code;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 标识为Java Bean
 * 用以运行时生成Getter Setter
 * 
 * @author luzj
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JavaBean {

    /**
     * 是否为 transient 字段生成 getter/setter
     * 默认不生成
     */
    boolean value() default false;
    
}

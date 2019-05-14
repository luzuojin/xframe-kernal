package dev.xframe.modular;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要注入的属性/参数/...etc
 * Field仅支持@ModularShare注入
 * @author luzj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ModularInject {
    
    boolean lazy() default false;
    
}

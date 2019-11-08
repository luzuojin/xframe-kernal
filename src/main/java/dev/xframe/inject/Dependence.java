package dev.xframe.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 
 * 显示标注bean之间的依赖关系
 * 某些Bean对Prototype的依赖暂时无法通过静态信息分析出来
 * @author luzj
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Dependence {

    Class<?>[] value() default {};
    
}

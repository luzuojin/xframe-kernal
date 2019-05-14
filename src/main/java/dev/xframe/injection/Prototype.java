package dev.xframe.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 
 * 对通过new关键字生成的对象进行注入
 * 框架会优先给Prototype增加补丁来实现注入
 * 要求多个prototype之间没有通过静态(static)方式的依赖关系
 * @author luzj
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Prototype {

}

package dev.xframe.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识某个Type对应的Bean需要合并成一个
 * 生成一个代理类(并把所有该Type的实现Bean放入一个List中)
 * 方法执行:
 *  无返回值的方法: 执行完List中所有的实现Bean
 *  有返回值的方法: Boolean类型遇到True终止执行, Object遇到非Null终止执行
 * @author luzj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Composite {
    
    boolean boolByTrue() default true;      //Boolean返回值是遇True终止, 否则遇False终止
    boolean ignoreError() default false;	//返回值为void时有效

}

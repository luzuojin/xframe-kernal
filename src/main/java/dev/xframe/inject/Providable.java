package dev.xframe.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 同时标识类及对应的接口或父类(可以只是本身)
 * 当该类或者@Providable的接口/父类有实现时 该实现类(@Providable)会被忽略
 * @author luzj
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Providable {

}

package dev.xframe.game.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * mark module sharable interface
 * 
 * @author luzj
 *
 */
@ModularScope
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModularShare {

}

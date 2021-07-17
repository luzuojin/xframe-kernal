package dev.xframe.game.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ModularScope
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModularComponent {

    ModuleType value() default ModuleType.TRANSIENT;
    
    Class<?> exports() default Class.class;
    
}

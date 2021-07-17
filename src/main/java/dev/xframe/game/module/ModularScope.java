package dev.xframe.game.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.xframe.inject.beans.ScopeType;

@ScopeType
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModularScope {

}

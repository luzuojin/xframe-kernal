package dev.xframe.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Module实例实现Agent接口, 同一个接口的所有Agent会组装到一个实例中, 使用ModularInject可以注入该Agent接口
 * @author luzj
 */
@ModularScope
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModularAgent {
    
    public boolean invokable() default true;       //是否生成对应的invoke方法 (如果为false 表明该Proxy仅仅是一个容器)
	public boolean boolByTrue() default true;
    public boolean ignoreError() default false;
	
}

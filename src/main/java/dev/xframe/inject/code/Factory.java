package dev.xframe.inject.code;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识一个接口为一个工厂类 只能在Interface中使用, Interface只能拥有一个方法(该方法即为创建该工厂所生产的对象的构造函数方法)
 * 需要一个对应的Annotation(value)来区别该工厂所产生的对象, 对应的Annotation应该只有一个属性, 该属性即为区别工厂所生产的对象的标识(key)
 * 工厂方法的第一个参数为构建的Key且不传入构建的实例中
 * @author luzj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Factory {
    
    /**
     * annotation identify on sub type
     * @return
     */
    Class<? extends Annotation> value();//区别生产对象的Annotation(该Annotation中需有且仅有一个属性(返回值可以为数组),该属性的值为构建生产对象的Key)
    
    /**
     * Default sub type
     * @return
     */
    Class<?> defaultType() default Class.class;
    
    /**
     * switchcase key use in sub type constructor
     * @return
     */
    boolean keyInConstructor() default false;
    
    boolean singleton() default false;
    
}

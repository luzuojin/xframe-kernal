package dev.xframe.inject.beans;

import dev.xframe.inject.Composite;
import dev.xframe.inject.code.Clazz;

import java.util.List;

/**
 * 发现Bean, 向特定的BeanIndexes中regist对应的Binder
 * GlobalBeanIndexes/ModularBeanIndexes默认均会调用BeanDiscovery(自定义的Scope自行调用)
 * 实现时需要根据BeanIndexes的offset/implemention来确认注册对应Scope的BeanBinder
 * 
 * BeanDiscovery在BeanContainer的integrate(加载)之前调用
 * 
 * 大部分的Bean只需要通过BeanRegistrator来注册就可以.
 * BeanRegistrator在对应的Bean执行相关逻辑时调用
 * @author luzj
 */
@Composite
public interface BeanDiscovery {
    /**
     * 调用时, 该实现类还没有执行integrate. 所以不能有对其他Bean的依赖调用
     * 没有执行integrate的Bean只是一个新生成的instance, 所有@Inject的字段均未处理. Loadable.load也未处理
     * @param scanned
     * @param reg
     */
    public void discover(List<Clazz> scanned, BeanRegistrator reg);

}

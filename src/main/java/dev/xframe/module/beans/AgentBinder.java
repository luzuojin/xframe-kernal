package dev.xframe.module.beans;

import java.util.function.Supplier;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.module.ModularAgent;
import dev.xframe.utils.XLambda;

public class AgentBinder extends ModularBinder {
    private Supplier<Object> factory;
    public AgentBinder(Class<?> master) {
        super(master, Injector.NIL);
        factory = XLambda.createByConstructor(buildAgentClass(master));
    }
    //append impl留给ModularListener来处理
    protected void integrate(Object bean, BeanDefiner definer) {
    }
    protected BeanBinder conflict(Object keyword, BeanBinder binder) {
        assert keyword instanceof Class;
        assert ((Class<?>) keyword).isAnnotationPresent(ModularAgent.class);
        assert binder instanceof ModularBinder;
        ((ModularBinder) binder).relate(this);
        return this;
    }
    protected Object newInstance() {
        return factory.get();
    }
    private Class<?> buildAgentClass(Class<?> c) {
        ModularAgent an = c.getAnnotation(ModularAgent.class);
        return SyntheticBuilder.buildClass(c, an.invokable(), an.ignoreError(), an.boolByTrue());
    }
    public void appendImpl(ModuleContainer mc, Object impl) {
        SyntheticBuilder.append(mc.getBean(getIndex()), impl);
    }
    public void removeImpl(ModuleContainer mc, Object impl) {
        SyntheticBuilder.remove(mc.getBean(getIndex()), impl);
    }
}

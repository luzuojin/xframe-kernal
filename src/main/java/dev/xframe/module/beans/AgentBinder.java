package dev.xframe.module.beans;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanFetcher;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.CompositeBuilder;
import dev.xframe.module.ModularAgent;
import dev.xframe.utils.XFactory;

public class AgentBinder extends ModularBinder implements ModularListener {
    public AgentBinder(Class<?> master) {
        super(master, Injector.NIL, XFactory.of(buildAgentClass(master)));
    }
    //append impl留给ModularListener来处理
    protected void integrate(Object bean, BeanFetcher fetcher) {
    }
    protected BeanBinder conflict(Object keyword, BeanBinder binder) {
        assert keyword instanceof Class;
        assert ((Class<?>) keyword).isAnnotationPresent(ModularAgent.class);
        assert binder instanceof ModularBinder;
        ((ModularBinder) binder).registListener(this);
        return this;
    }
    private static Class<?> buildAgentClass(Class<?> c) {
        ModularAgent an = c.getAnnotation(ModularAgent.class);
        return CompositeBuilder.buildClass(c, an.invokable(), an.ignoreError(), an.boolByTrue());
    }
	@Override
	public void onModuleLoaded(ModuleContainer mc, ModularBinder binder, Object module) {
		CompositeBuilder.append(mc.getBean(getIndex()), module);
	}
	@Override
	public void onModuleUnloaded(ModuleContainer mc, ModularBinder binder, Object module) {
		CompositeBuilder.remove(mc.getBean(getIndex()), module);
	}
}

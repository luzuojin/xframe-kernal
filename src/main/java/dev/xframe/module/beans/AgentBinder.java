package dev.xframe.module.beans;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.module.ModularAgent;
import dev.xframe.utils.XLambda;

public class AgentBinder extends ModularBinder implements ModularListener {
    public AgentBinder(Class<?> master) {
        super(master, Injector.NIL, XLambda.createByConstructor(buildAgentClass(master)));
    }
    //append impl留给ModularListener来处理
    protected void integrate(Object bean, BeanDefiner definer) {
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
        return SyntheticBuilder.buildClass(c, an.invokable(), an.ignoreError(), an.boolByTrue());
    }
	@Override
	public void onModuleLoaded(ModuleContainer mc, ModularBinder binder, Object module) {
		SyntheticBuilder.append(mc.getBean(getIndex()), module);
	}
	@Override
	public void onModuleUnloaded(ModuleContainer mc, ModularBinder binder, Object module) {
		SyntheticBuilder.remove(mc.getBean(getIndex()), module);
	}
}

package dev.xframe.module.beans;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.Injector;
import dev.xframe.module.ModularHelper;
import dev.xframe.module.ModularScope;
import dev.xframe.utils.XReflection;

public class ModularBinder extends BeanBinder.Classic {
	
	protected ModularInvoker invoker;
	protected List<ModularListener> listeners = new LinkedList<>();

	public ModularBinder(Class<?> master, Injector injector) {
		super(master, injector);
	}
	protected ModularBinder(Class<?> master, Injector injector, Supplier<?> factory) {
	    super(master, injector, factory);
	}

	public boolean isResident() {
		return ModularHelper.isResidentModularClass(master);
	}
	
	public boolean isTransient() {
		return ModularHelper.isTransientModularClass(master);
	}
	
	public void makeComplete(ModularIndexes indexes) {
		invoker = MInvokerBuilder.build(master, indexes);
	}
	
	@Override
	protected List<Class<?>> getKeywords() {
		return XReflection.getAssigners(master).stream().filter(c->ModularHelper.isModularClass((Class<?>)c)).collect(Collectors.toList());
	}

	@Override
	protected boolean injectable(Field field) {
		return ModularHelper.isModularSharable(field.getType(), field.getDeclaringClass());
	}
	
	public ModularInvoker getInvoker() {
		return invoker;
	}

	@SuppressWarnings("unchecked")
    public <T> T getModuleFrom(ModuleContainer mc) {
		return (T) mc.getBean(getIndex());
	}

	public String getName() {
		return master.getName();
	}

	public void registListener(ModularListener listener) {
		this.listeners.add(listener);
	}
	
	public void onLoaded(ModuleContainer mc, Object module) {
		for (ModularListener listener : listeners) {
			listener.onModuleLoaded(mc, this, module);
		}
	}
	public void onUnloaded(ModuleContainer mc, Object module) {
		for (ModularListener listener : listeners) {
			listener.onModuleUnloaded(mc, this, module);
		}
	}
    @Override
    protected Class<?> scope() {
        return ModularScope.class;
    }
}

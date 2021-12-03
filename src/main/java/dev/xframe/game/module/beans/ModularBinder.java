package dev.xframe.game.module.beans;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.game.module.ModularHelper;
import dev.xframe.game.module.ModularScope;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.Injector;
import dev.xframe.utils.XFactory;
import dev.xframe.utils.XReflection;

public class ModularBinder extends BeanBinder.Classic {
	
	protected ModularInvoker invoker;
	protected List<ModularListener> listeners = new LinkedList<>();

	public ModularBinder(Class<?> master, Injector injector) {
		super(master, injector);
	}
	protected ModularBinder(Class<?> master, Injector injector, XFactory<?> factory) {
	    super(master, injector, factory);
	}

	public boolean isResident() {
		return ModularHelper.isResidentModularClass(master);
	}
	
	public boolean isTransient() {
		return ModularHelper.isTransientModularClass(master);
	}
	
	public void makeComplete(ModularIndexes indexes, MInvokerFactory miFactory) {
		invoker = miFactory.makeInvoker(master, indexes);
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

    public <T> T getModuleFrom(ModuleContainer mc) {
		return mc.getBean(getIndex());
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

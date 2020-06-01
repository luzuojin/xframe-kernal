package dev.xframe.module.beans;

import java.util.List;
import java.util.stream.Collectors;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.Injector;
import dev.xframe.module.ModularAgent;
import dev.xframe.module.ModularComponent;
import dev.xframe.module.ModularHelper;
import dev.xframe.module.Module;
import dev.xframe.module.ModuleType;
import dev.xframe.utils.XReflection;

public class ModularBinder extends BeanBinder.Classic {
	
	protected ModularInvoker invoker;

	public ModularBinder(Class<?> master, Injector injector) {
		super(master, injector);
	}

	public boolean isResident() {
		return master.isAnnotationPresent(ModularAgent.class)
		   || (master.isAnnotationPresent(Module.class) && master.getAnnotation(Module.class).value() == ModuleType.RESIDENT)
		   || (master.isAnnotationPresent(ModularComponent.class) && master.getAnnotation(ModularComponent.class).value() == ModuleType.RESIDENT)
		   ;
	}
	
	public boolean isTransient() {
		return (master.isAnnotationPresent(Module.class) && master.getAnnotation(Module.class).value() == ModuleType.TRANSIENT) ||
			   (master.isAnnotationPresent(ModularComponent.class) && master.getAnnotation(ModularComponent.class).value() == ModuleType.TRANSIENT)
			   ;
	}
	
	public void buildInvoker(BeanIndexing indexing) {
		invoker = MInvokerBuilder.build(master, indexing);
	}
	
	@Override
	protected List<?> getKeywords() {
		return XReflection.getAssigners(master).stream().filter(c->ModularHelper.isModularClass((Class<?>)c)).collect(Collectors.toList());
	}

	public ModularInvoker getInvoker() {
		return invoker;
	}

	public <T> T getModuleFrom(ModuleContainer mc) {
		return (T) mc.getBean(getIndex());
	}

	public String getName() {
		return master.getName();
	}
	
}

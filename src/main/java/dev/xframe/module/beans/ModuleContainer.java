package dev.xframe.module.beans;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanContainer;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.module.ModuleType;
import dev.xframe.utils.XLogger;

public class ModuleContainer extends BeanContainer  {
	
	//global beans
	BeanDefiner gDefiner;
	
	public ModuleContainer setup(BeanDefiner gDefiner, ModularIndexes indexes) {
		super.setup(indexes);
		this.gDefiner = gDefiner;
		return this;
	}

	@Override
	public synchronized Object define(int index) {
		if(index >= ModularIndexes.OFFSET) {
			return super.define(index);
		}
		return gDefiner.define(index);
	}

	public void loadModules(ModuleType type) {
		if(type == ModuleType.RESIDENT) {
			loadModules(((ModularIndexes)indexes).residents);
		} else {
			loadModules(((ModularIndexes)indexes).transients);
		}
	}
	private boolean loadModules(ModularBinder[] binders) {
		for(ModularBinder binder : binders) {
			integrate(binder);//load过程由Bean的初始化完成,逻辑走loadBeanExec
		}
		return true;
	}
	
	//调用这个方法会确认加载flag
	protected void loadBeanExec(BeanBinder binder, Object bean) {
		super.loadBeanExec(binder, bean);
		((ModularBinder)binder).getInvoker().invokeLoad(this);
	}

	public void unloadModules(ModuleType type) {
		if(type == ModuleType.RESIDENT) {
			unloadModules(((ModularIndexes)indexes).residents);
		} else {
			unloadModules(((ModularIndexes)indexes).transients);
		}
	}
	private boolean unloadModules(ModularBinder[] binders) {
		for (ModularBinder binder : binders) {
			binder.getInvoker().invokeUnload(this);
		}
		return true;
	}
	
	public void saveModules() {
		saveModules(((ModularIndexes)indexes).residents);
		saveModules(((ModularIndexes)indexes).transients);
	}
	private void saveModules(ModularBinder[] binders) {
		for (ModularBinder binder : binders) {
			try {
				binder.getInvoker().invokeSave(this);
			} catch (Exception e) {
				XLogger.warn("Save module[" + binder.getName() + "] throws: ", e);
			}
		}
	}
	
	public boolean isModuleLoaded(int index) {
		return getFlag(index);
	}

//	public ModuleTypeLoader getModuleLoader(Class<?> moduleType) {
//		return (ModularBinder) indexes.getBinder(indexes.getIndex(moduleType));
//	}
//	
//	public <T> T getModule(ModuleContainer mc, Class<T> moduleType) {
//		return getModuleLoader(moduleType).load(mc);
//	}

}

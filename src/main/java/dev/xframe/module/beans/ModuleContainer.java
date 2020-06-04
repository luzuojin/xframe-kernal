package dev.xframe.module.beans;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanContainer;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.module.ModuleType;

public class ModuleContainer extends BeanContainer  {
	
	//global beans
	private BeanDefiner gDefiner;
	
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

	public synchronized void loadModules(ModuleType type) {
		if(type == ModuleType.RESIDENT) {
			loadModules(((ModularIndexes)indexes).residents);
		} else {
			loadModules(((ModularIndexes)indexes).transients);
		}
	}
	private void loadModules(ModularBinder[] binders) {
		for(ModularBinder binder : binders) {
			integrate(binder);//load过程由Bean的初始化完成,逻辑走loadBeanExec
		}
	}
	
	@Override //调用这个方法会确认是否已经加载过的flag
	protected void loadBeanExec(BeanBinder binder, Object bean) {
		super.loadBeanExec(binder, bean);
		((ModularBinder)binder).getInvoker().invokeLoad(this);//正常可以由ModularBinder在integrate方法中调用. 这里因为unload/save均在此类
	}

	public synchronized void unloadModules(ModuleType type) {
		if(type == ModuleType.RESIDENT) {
			unloadModules(((ModularIndexes)indexes).residents);
		} else {
			unloadModules(((ModularIndexes)indexes).transients);
		}
	}
	private void unloadModules(ModularBinder[] binders) {
		for (ModularBinder binder : binders) {
			binder.getInvoker().invokeUnload(this);
			this.clearModule(binder);
		}
	}
	//清空Bean
    private void clearModule(ModularBinder binder) {
        this.setBean(binder.getIndex(), null);
        this.setFlag(binder.getIndex(), false);
    }
	
	public void saveModules() {
		saveModules(((ModularIndexes)indexes).residents);
		saveModules(((ModularIndexes)indexes).transients);
	}
	private void saveModules(ModularBinder[] binders) {
		for (ModularBinder binder : binders) {
			binder.getInvoker().invokeSave(this);//异常在ModularInvoker已经捕获
		}
	}
	
	public boolean isModuleLoaded(int index) {
		return getFlag(index);
	}

}

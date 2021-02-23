package dev.xframe.module.beans;

import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanContainer;
import dev.xframe.inject.beans.BeanFetcher;
import dev.xframe.module.ModuleType;

public class ModuleContainer extends BeanContainer  {
	
	//global beans
	private BeanFetcher gFetcher;
	
	public ModuleContainer(BeanFetcher gFetcher, ModularIndexes indexes) {
		super(indexes);
		this.gFetcher = gFetcher;
	}

	@Override
	public synchronized Object fetch(int index) {
		if(indexes.isValidIndex(index)) {
			return super.fetch(index);
		}
		return gFetcher.fetch(index);
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
		ModularBinder mbinder = (ModularBinder)binder;
        mbinder.getInvoker().invokeLoad(this);
		mbinder.onLoaded(this, bean);
	}

	public synchronized void unloadModules(ModuleType type) {
		if(type == ModuleType.RESIDENT) {
			unloadModules(((ModularIndexes)indexes).residents);
		} else {
			unloadModules(((ModularIndexes)indexes).transients);
		}
	}
	//unload需要倒过来处理
	private void unloadModules(ModularBinder[] binders) {
	    for (int i=binders.length-1; i>=0; i--) {
            this.unloadModule(binders[i]);
        }
	}
    private void unloadModule(ModularBinder binder) {
        int bIndex = binder.getIndex();
        Object ex = this.getBean(bIndex);
        binder.getInvoker().invokeUnload(this);
        //清空Container中的ref
        this.setBean(bIndex, null);
        this.setFlag(bIndex, false);
        //清空Agent...中的ref
        binder.onUnloaded(this, ex);
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

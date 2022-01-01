package dev.xframe.game.module.beans;

import dev.xframe.game.module.ModuleType;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanContainer;
import dev.xframe.inject.beans.BeanProvider;

public class ModuleContainer extends BeanContainer  {
	
	//global beans
	private BeanProvider gProvider;
	
	public ModuleContainer(BeanProvider gProvider, ModularIndexes indexes) {
	    super(indexes);
	    this.gProvider = gProvider;
	}

	@Override
	public synchronized Object fetch(int index) {
	    if(indexes.isValidIndex(index)) {
	        return super.fetch(index);
	    }
	    return gProvider.getBean(index);
	}

	public <T> T getBean(int index) {
	    if(indexes.isValidIndex(index)) {
	        return super.getBean(index);
	    }
	    return gProvider.getBean(index);
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
        mbinder.getInvoker().invokeLoad(this, bean);
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
        binder.getInvoker().invokeUnload(this, ex);
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
			binder.getInvoker().invokeSave(this, getBean(binder.getIndex()));//异常在ModularInvoker已经捕获
		}
	}
	
	public void tickModules() {
	    tickModules(((ModularIndexes)indexes).residents);
	    tickModules(((ModularIndexes)indexes).transients);
    }
    private void tickModules(ModularBinder[] binders) {
        for (ModularBinder binder : binders) {
            binder.getInvoker().invokeTick(this, getBean(binder.getIndex()));//异常在ModularInvoker已经捕获
        }
    }
	
	public boolean isModuleLoaded(int index) {
		return getFlag(index);
	}

}

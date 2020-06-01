package dev.xframe.module.beans;

import dev.xframe.module.ModularMethods;

/**
 * @see ModularMethods 
 * @author luzj
 */
public interface ModularInvoker {
	
	public void invokeLoad(ModuleContainer mc);
	public void invokeUnload(ModuleContainer mc);
	public void invokeSave(ModuleContainer mc);

}

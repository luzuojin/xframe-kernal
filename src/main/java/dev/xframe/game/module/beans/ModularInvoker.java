package dev.xframe.game.module.beans;

import dev.xframe.game.module.ModularMethods;

/**
 * @see ModularMethods 
 * @author luzj
 */
public interface ModularInvoker {
	
	public void invokeLoad(ModuleContainer mc);
	public void invokeUnload(ModuleContainer mc);
	public void invokeSave(ModuleContainer mc);
	public void invokeTick(ModuleContainer mc);

}

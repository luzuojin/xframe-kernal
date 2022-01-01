package dev.xframe.game.module.beans;

import dev.xframe.game.module.ModularMethods;

/**
 * @see ModularMethods 
 * @author luzj
 */
public interface ModularInvoker {

	ModularInvoker Empty = new ModularInvoker() {
		public void invokeLoad(ModuleContainer mc, Object module) {}
		public void invokeUnload(ModuleContainer mc, Object module) {}
		public void invokeSave(ModuleContainer mc, Object module) {}
		public void invokeTick(ModuleContainer mc, Object module) {}
	};
	
	void invokeLoad(ModuleContainer mc, Object module);
	void invokeUnload(ModuleContainer mc, Object module);
	void invokeSave(ModuleContainer mc, Object module);
	void invokeTick(ModuleContainer mc, Object module);

}

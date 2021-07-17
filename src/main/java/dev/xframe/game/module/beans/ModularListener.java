package dev.xframe.game.module.beans;

interface ModularListener {
	
	void onModuleLoaded(ModuleContainer mc, ModularBinder binder, Object module);
	void onModuleUnloaded(ModuleContainer mc, ModularBinder binder, Object module);

}

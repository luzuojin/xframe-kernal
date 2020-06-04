package dev.xframe.module.beans;

public interface ModularListener {
    
    public void onModuleLoaded(ModuleContainer mc, ModularBinder binder, Object module);
    
    public void onModuleUnload(ModuleContainer mc, ModularBinder binder, Object module);

}

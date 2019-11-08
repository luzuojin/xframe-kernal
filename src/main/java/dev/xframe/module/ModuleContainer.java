package dev.xframe.module;

public interface ModuleContainer {
    
    public boolean load(ModuleType type);
    
    public boolean unload(ModuleType type);
    
    public boolean save();
    
    public boolean isLoaded(ModuleType type);
    
    /**
     * 是否需要在超时时卸载内存数据
     * @param type
     * @return
     */
	public boolean unloadable(ModuleType type);

}

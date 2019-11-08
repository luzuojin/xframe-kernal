package dev.xframe.module;

/**
 * 
 * 每一个Module Type对应一个Loader
 * @author luzj
 *
 */
public interface ModuleTypeLoader {
    
    public <T> T load(ModuleContainer container);
    
    public static interface IModuleLoader {
        Object _loadModule(int n);
    }
    
}

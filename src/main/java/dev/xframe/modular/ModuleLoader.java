package dev.xframe.modular;

import dev.xframe.injection.Synthetic;

@Synthetic
public interface ModuleLoader {
    
    /**
     * 从ModuleContainer中加载某个模块
     * @param container
     * @param clazz
     * @return
     */
    public <T> T loadModule(ModuleContainer container, Class<T> clazz);
    
}

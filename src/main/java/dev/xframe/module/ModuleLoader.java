package dev.xframe.module;

import dev.xframe.inject.Synthetic;

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

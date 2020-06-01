package dev.xframe.module;

import dev.xframe.module.beans.ModuleContainer;

/**
 * 
 * 每一个Module Type对应一个Loader
 * @author luzj
 *
 */
@FunctionalInterface
public interface ModuleTypeLoader {
    
    public <T> T load(ModuleContainer container);
    
}

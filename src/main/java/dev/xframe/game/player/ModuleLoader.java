package dev.xframe.game.player;

import dev.xframe.inject.Composite;

@Composite
public interface ModuleLoader {
    
    /**
     * 通过Player加载某个模块
     * @return
     */
    public <T> T loadModule(Player player, Class<T> clazz);
    
}

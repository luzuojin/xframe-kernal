package dev.xframe.test.game;

import dev.xframe.game.event.RegistryAdapter;
import dev.xframe.module.ModularMethods;

public interface TInventory extends RegistryAdapter {
    
    @ModularMethods.Load
    public void load(TPlayer player);
    
    @ModularMethods.Save
    @ModularMethods.Unload
    public void save();
    
}

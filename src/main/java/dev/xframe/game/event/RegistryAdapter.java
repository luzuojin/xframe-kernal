package dev.xframe.game.event;

import dev.xframe.event.Registrator;
import dev.xframe.event.Registry;
import dev.xframe.module.ModularMethods;

public interface RegistryAdapter {
    
    @ModularMethods.Load
    public default void z_regist(Registrator registrator) {
        Registry.regist(getClass(), this, registrator);
    }
    
    @ModularMethods.Unload
    public default void a_unregist(Registrator registrator) {
        Registry.unregist(getClass(), registrator);
    }

}

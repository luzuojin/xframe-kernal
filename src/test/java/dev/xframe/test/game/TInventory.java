package dev.xframe.test.game;

import dev.xframe.event.Registry;
import dev.xframe.module.ModularMethods;
import dev.xframe.module.Module;
import dev.xframe.module.ModuleType;

@Module(ModuleType.TRANSIENT)
public interface TInventory {
    
    @ModularMethods.Load
    public void load(TPlayer player);
    
    @ModularMethods.Load
    default void z_regist(TPlayer registrator, TAgent agent) {
        Registry.regist(this.getClass(), this, registrator);
    }
    
    @ModularMethods.Save
    @ModularMethods.Unload
    public void save();
    
    @ModularMethods.Unload
    default void a_unregist(TPlayer registrator) {
        Registry.unregist(this.getClass(), registrator);
    }

}

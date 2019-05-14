package dev.xframe.test.game;

import dev.xframe.event.Registry;
import dev.xframe.modular.ModularInject;
import dev.xframe.modular.ModularMethods;
import dev.xframe.modular.Module;
import dev.xframe.modular.ModuleType;

@Module(ModuleType.TRANSIENT)
public interface TInventory {
    
    @ModularMethods.Load
    public void load(@ModularInject TPlayer player);
    
    @ModularMethods.Load
    default void z_regist(@ModularInject TPlayer registrator, @ModularInject TAgent agent) {
        Registry.regist(this.getClass(), this, registrator);
    }
    
    @ModularMethods.Save
    @ModularMethods.Unload
    public void save();
    
    @ModularMethods.Unload
    default void a_unregist(@ModularInject TPlayer registrator) {
        Registry.unregist(this.getClass(), registrator);
    }

}

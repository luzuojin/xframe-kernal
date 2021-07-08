package dev.xframe.game.action;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.Player;
import dev.xframe.utils.XGeneric;

abstract class IModularAction<V> {
    
    MTypedLoader mTyped;
    
    void ensureMTyped(Player player) {
        if(mTyped == null) {
            ActionBuilder.of(getClass()).makeCompelte(this, player);
        }
    }
    
    static Class<?> getModuleType(Class<?> cls) {
        return XGeneric.parse(cls, IModularAction.class).getByName("V");
    }

}

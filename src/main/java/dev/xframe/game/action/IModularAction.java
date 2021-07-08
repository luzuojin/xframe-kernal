package dev.xframe.game.action;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.utils.XGeneric;

abstract class IModularAction<V> {
    
    MTypedLoader mTypedLoader;
    
    static Class<?> getModuleType(Class<?> cls) {
        return XGeneric.parse(cls, IModularAction.class).getByName("V");
    }

}

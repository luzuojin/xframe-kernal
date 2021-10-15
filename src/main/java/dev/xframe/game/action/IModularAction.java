package dev.xframe.game.action;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.utils.XGeneric;

abstract class IModularAction<V> {
    
    MTypedLoader mTyped;
    
    static Class<?> getModuleType(Class<?> cls) {
        return XGeneric.parse(cls, IModularAction.class).getOnlyType();
    }

}

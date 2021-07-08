package dev.xframe.game.cmd;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.Player;
import dev.xframe.utils.XGeneric;

public abstract class ModularAction<T extends Player, V, M> implements Action<T, M> {

    MTypedLoader mTypedLoader;
    
    public static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, ModularAction.class).getByName("V");
    }

    @Override
    public final void exec(T player, M msg) throws Exception {
        exec(player, mTypedLoader.load(player), msg);
    }

    public abstract void exec(T player, V module, M msg) throws Exception;
}

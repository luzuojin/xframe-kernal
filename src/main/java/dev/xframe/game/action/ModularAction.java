package dev.xframe.game.action;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.Player;

public abstract class ModularAction<T extends Player, V, M> implements Action<T, M> {

    MTypedLoader mTyped;

    @Override
    public final void exec(T player, M msg) throws Exception {
        exec(player, mTyped.load(player), msg);
    }

    public abstract void exec(T player, V module, M msg) throws Exception;
    
}

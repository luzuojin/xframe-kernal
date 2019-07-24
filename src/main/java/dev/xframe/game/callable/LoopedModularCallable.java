package dev.xframe.game.callable;

import dev.xframe.action.RunnableAction;
import dev.xframe.game.player.ModularPlayer;

public interface LoopedModularCallable<T extends ModularPlayer, V> extends ModularCallable<T, V> {

    default void call(final T player, final V module) {
        RunnableAction.of(player.loop(), ()->exec(player, module)).checkin();
    }

    public void exec(T player, V module);

}

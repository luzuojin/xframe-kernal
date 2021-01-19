package dev.xframe.game.callable;

import dev.xframe.action.RunnableAction;
import dev.xframe.game.player.Player;

public interface LoopedModularCallable<T extends Player, V> extends ModularCallable<T, V> {

    default void call(final T player) {
        RunnableAction.of(player.loop(), ()->exec(player)).checkin();
    }

}

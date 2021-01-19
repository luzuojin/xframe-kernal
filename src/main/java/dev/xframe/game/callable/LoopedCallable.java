package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableBuilders.setup0;

import dev.xframe.action.RunnableAction;
import dev.xframe.game.player.Player;

public interface LoopedCallable<T extends Player> extends PlayerCallable<T>{

    @Override
    default void call(final T player) {
        RunnableAction.of(player.loop(), ()->exec(setup0(player, this))).checkin();
    }

}

package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableBuilders.setup0;

import dev.xframe.game.player.Player;
import dev.xframe.task.RunnableTask;

public interface LoopedCallable<T extends Player> extends PlayerCallable<T>{

    @Override
    default void call(final T player) {
        RunnableTask.of(player.loop(), ()->exec(setup0(player, this))).checkin();
    }

}

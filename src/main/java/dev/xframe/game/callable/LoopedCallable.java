package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableInjector.doInject;

import dev.xframe.game.player.Player;
import dev.xframe.task.RunnableTask;

public interface LoopedCallable<T extends Player> extends PlayerCallable<T>{

    @Override
    default void call(final T player) {
        RunnableTask.of(player.loop(), ()->exec(doInject(player, this))).checkin();
    }

}

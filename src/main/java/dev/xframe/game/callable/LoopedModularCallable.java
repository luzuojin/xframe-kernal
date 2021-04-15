package dev.xframe.game.callable;

import dev.xframe.game.player.Player;
import dev.xframe.task.RunnableTask;

public interface LoopedModularCallable<T extends Player, V> extends ModularCallable<T, V> {

    default void call(final T player) {
        RunnableTask.of(player.loop(), ()->exec(player)).checkin();
    }

}

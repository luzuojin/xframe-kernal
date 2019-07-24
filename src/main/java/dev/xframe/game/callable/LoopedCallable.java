package dev.xframe.game.callable;

import dev.xframe.action.RunnableAction;
import dev.xframe.game.player.Player;

public interface LoopedCallable<T extends Player> extends Callable<T>{

    @Override
    default void call(final T player) {
        RunnableAction.of(player.loop(), ()->exec(player)).checkin();
    }

    public void exec(T player);

}

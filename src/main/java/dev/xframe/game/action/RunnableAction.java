package dev.xframe.game.action;

import dev.xframe.game.player.Player;

/**
 * 接受一个runnable为msg
 * @param <T>
 */
class RunnableAction<T extends Player> implements Action<T, Runnable<T>> {
    @Override
    public void exec(T player, Runnable<T> msg) throws Exception {
        msg.run(player);
    }
}

package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableInjector.doInject;

import dev.xframe.game.player.Player;

/**
 * 
 * @author luzj
 *
 */
public interface PlayerCallable<T extends Player> {

    default void call(T player) {
        exec(doInject(player, this));
    }

    void exec(T player);
    
}

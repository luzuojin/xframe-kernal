package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableBuilders.setup0;

import dev.xframe.game.player.Player;

/**
 * 
 * @author luzj
 *
 */
public interface PlayerCallable<T extends Player> {

    default void call(T player) {
        exec(setup0(player, this));
    }

    void exec(T player);
    
}

package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableBuilders.setup1;

import dev.xframe.game.player.Player;

/**
 * 
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
public interface ModularCallable<T extends Player, V> extends PlayerCallable<T> {
	
    default void call(T player) {
        exec(player);
    }
    
    default void exec(T player) {
        exec(player, setup1(player, this));
    }

    void exec(T player, V module);

}

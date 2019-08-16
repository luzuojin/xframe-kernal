package dev.xframe.game.callable;

import dev.xframe.game.player.Player;

/**
 * 
 * @author luzj
 *
 */
public interface PlayerCallable<T extends Player> {

    public void call(T player);
    
}

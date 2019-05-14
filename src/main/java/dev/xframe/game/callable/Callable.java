package dev.xframe.game.callable;

import dev.xframe.game.player.Player;

/**
 * 
 * @author luzj
 *
 */
public interface Callable<T extends Player> {

    public void call(T player);
    
}

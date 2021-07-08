package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;


/**
 * player action with msg
 */
public interface Action<T extends Player, M> {
    
    void exec(T player, M msg) throws Exception;

}

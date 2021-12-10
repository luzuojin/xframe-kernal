package dev.xframe.game.action;

import dev.xframe.game.player.Player;

/**
 * player action with msg
 * msg can be data Pojo or Runnable instance
 */
public interface Action<T extends Player, M> {
    
    void exec(T player, M msg) throws Exception;

}

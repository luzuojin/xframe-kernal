package dev.xframe.game;

import dev.xframe.game.player.Player;

public interface PlayerLoader {
    
    public boolean exists(long playerId);
    
    public <T extends Player> T getPlayer(long playerId);

}

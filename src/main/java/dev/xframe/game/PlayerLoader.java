package dev.xframe.game;

import dev.xframe.game.player.Player;
import dev.xframe.inject.Providable;

@Providable
public interface PlayerLoader {
    
    public boolean exists(long playerId);
    
    public <T extends Player> T getPlayer(long playerId);

}

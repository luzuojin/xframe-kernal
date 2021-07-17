package dev.xframe.game.player;

import dev.xframe.inject.Providable;

@Providable
public interface PlayerProvider {
    
    public boolean exists(long playerId);
    
    public <T extends Player> T get(long playerId);

}

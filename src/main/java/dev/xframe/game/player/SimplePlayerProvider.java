package dev.xframe.game.player;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Providable;

@Bean
@Providable
public class SimplePlayerProvider implements PlayerProvider {
    
    @Inject(required=false)
    private PlayerContext playerCtx;
    
    @Override
    public boolean exists(long playerId) {
        return playerCtx != null && playerCtx.exists(playerId);
    }
    
    @Override
    public <T extends Player> T get(long playerId) {
        return playerCtx == null ? null : playerCtx.getPlayerWithLoad(playerId);
    }

}

package dev.xframe.game.player;

import dev.xframe.game.PlayerLoader;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Providable;

@Bean
@Providable
public class SimplePlayerLoader implements PlayerLoader {
    
    @Inject(nullable=true)
    private PlayerContext playerCtx;
    
    @Override
    public boolean exists(long playerId) {
        return playerCtx != null && playerCtx.exists(playerId);
    }
    
    @Override
    public <T extends Player> T getPlayer(long playerId) {
        return playerCtx == null ? null : playerCtx.getPlayerWithLoad(playerId);
    }

}

package dev.xframe.game.player;

import dev.xframe.game.PlayerLoader;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Providable;

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

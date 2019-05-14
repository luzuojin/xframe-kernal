package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Providable;
import dev.xframe.net.codec.IMessage;

@Bean
@Providable
public class PlayerCmdInvoker<T extends Player> {
    
    public void invoke(PlayerCommand<T> cmd, T player, IMessage req) throws Exception {
        cmd.exec(player, req);
    }

}

package dev.xframe.game.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.game.player.Player;
import dev.xframe.injection.Bean;
import dev.xframe.injection.Providable;
import dev.xframe.net.codec.IMessage;
import dev.xframe.tools.XStrings;

@Bean
@Providable
public class PlayerCmdInvoker<T extends Player> {
    
    protected static final Logger logger = LoggerFactory.getLogger(PlayerCmdInvoker.class);
    
    public final void invoke(PlayerCommand<T> cmd, T player, IMessage req) {
        try {
            doInvoke(cmd, player, req);
        } catch (Throwable e) {
            onExCaught(cmd, player, e);
        }
    }

    protected void doInvoke(PlayerCommand<T> cmd, T player, IMessage req) throws Exception {
        doExec(cmd, player, req);
    }

    protected void doExec(PlayerCommand<T> cmd, T player, IMessage req) throws Exception {
        cmd.exec(player, req);
    }

    protected void onExCaught(PlayerCommand<T> cmd, T player, Throwable e) {
        logger.error("invoke player[{}] cmd[{}] error:\n {}", player.getPlayerId(), cmd.getClazz(), XStrings.getStackTrace(e));
    }

}

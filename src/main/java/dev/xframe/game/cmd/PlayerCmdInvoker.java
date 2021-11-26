package dev.xframe.game.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.game.player.Player;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import dev.xframe.utils.XStrings;

@Bean
@Providable
public class PlayerCmdInvoker<T extends Player, E> {
    
    protected static final Logger logger = LoggerFactory.getLogger(PlayerCmdInvoker.class);
    
    public final void invoke(PlayerCmd<T, E> cmd, T player, E req) {
        try {
            doInvoke(cmd, player, req);
        } catch (Throwable e) {
            onExCaught(cmd, player, e);
        }
    }

    protected void doInvoke(PlayerCmd<T, E> cmd, T player, E req) throws Exception {
        doExec(cmd, player, req);
    }

    protected void doExec(PlayerCmd<T, E> cmd, T player, E req) throws Exception {
        cmd.exec(player, req);
    }

    protected void onExCaught(PlayerCmd<T, E> cmd, T player, Throwable e) {
        logger.error("invoke player[{}] cmd[{}] error:\n {}", player.id(), cmd.getClazz(), XStrings.getStackTrace(e));
    }

}

package dev.xframe.game.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.game.PlayerLoader;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.net.cmd.Command;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public abstract class PlayerCmd<T extends Player> implements Command {
    
    protected static Logger logger = LoggerFactory.getLogger(PlayerCmd.class);
    
    @Inject
    private PlayerLoader playerLoader;
    
    @SuppressWarnings("unchecked")
    public final void execute(Session session, IMessage req) throws Exception {
        long playerId = req.getId();
        Player player = playerLoader.getPlayer(playerId);
        if(player != null) {
            execute0((T) player, req);
        } else {
            logger.error("Player not found [" + playerId + "]");
        }
    }
    
    //for Looped Or Direct
    protected abstract void execute0(T player, IMessage req) throws Exception;
    
    public abstract void exec(T player, IMessage req) throws Exception;

    protected Class<?> getClazz() {
        return this.getClass();
    }
    
}

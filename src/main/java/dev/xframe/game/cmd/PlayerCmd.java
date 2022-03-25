package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.game.player.PlayerProvider;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.net.cmd.Command;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XGeneric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PlayerCmd<T extends Player, M> implements Command, Loadable {
    
    protected static Logger logger = LoggerFactory.getLogger(PlayerCmd.class);
    
    @Inject
    PlayerProvider pProvider;
    
    ExplicitMsgParser eParser;
    
    @Override
    public final void load() {
        eParser = ExplicitMsgParsers.newParser(getExplicitCls());
        onLoad();
    }

    protected void onLoad() {
    }

    protected Class<?> getExplicitCls() {
        return XGeneric.parse(getClass(), PlayerCmd.class).getByIndex(1);
    }

    @SuppressWarnings("unchecked")
    public final void execute(Session session, IMessage req) throws Exception {
        final long id = req.getId();
        Player player = pProvider.get(id);
        if(player != null) {
            execute0((T) player, eParser.parse(req));
        } else {
            logger.error("Player not found [" + id + "]");
        }
    }
    
    //for Looped Or Direct
    protected abstract void execute0(T player, M req) throws Exception;
    
    public abstract void exec(T player, M req) throws Exception;

    protected Class<?> getClazz() {
        return this.getClass();
    }
    
}

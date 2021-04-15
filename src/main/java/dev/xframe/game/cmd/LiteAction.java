package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;

public abstract class LiteAction<T extends Player, V, L> extends Action<T, V> {

    LiteParser parser;//该类是prototype 所以parser构建放在调用的地方 (@see ActionCmd)

    public final void exec(T player, V module, IMessage req) throws Exception {
        exec(player, module, parser.<L>parse(req.getBody()));
    }
    
    public abstract void exec(T player, V module, L req) throws Exception;

}

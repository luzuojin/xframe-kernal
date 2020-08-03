package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;

public abstract class PlayerCmdLiteAction<T extends Player, V, L> extends PlayerCmdAction<T, V> {

    LiteParser parser;//该类是prototype 所以parser构建放在调用的地方 (@see PlayerCmdActionCmd)

    public final void exec(T player, V module, IMessage req) throws Exception {
        exec(player, module, parser.<L>parse(req.getBody()));
    }
    
    public abstract void exec(T player, V module, L req) throws Exception;

}

package dev.xframe.game.cmd;

import com.google.protobuf.MessageLite;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.module.ModularInject;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.LiteParser;

public abstract class PlayerCmdLiteAction<T extends ModularPlayer, V, L extends MessageLite> extends PlayerCmdAction<T, V> {

    LiteParser parser;//该类是prototype 所以parser构建放在调用的地方 (@see PlayerCmdActionCmd)

    public final void exec(T player, @ModularInject V module, IMessage req) throws Exception {
        exec(player, module, parser.<L>parse(req.getBody()));
    }
    
    public abstract void exec(T player, V module, L req) throws Exception;

}

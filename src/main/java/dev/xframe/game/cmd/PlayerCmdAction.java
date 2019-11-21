package dev.xframe.game.cmd;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XGeneric;

public abstract class PlayerCmdAction<T extends ModularPlayer, V> {
    
    public static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, PlayerCmdAction.class).getByName("V");
    }
    
    public abstract void exec(T player, V module, IMessage req) throws Exception;

}

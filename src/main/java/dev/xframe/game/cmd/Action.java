package dev.xframe.game.cmd;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XGeneric;


/**
 * Player scope Task(Prototype)
 */
public abstract class Action<T extends Player, V> {
    
    public static Class<?> getModuleType(Class<?> clazz) {
        return XGeneric.parse(clazz, Action.class).getByName("V");
    }
    
    public abstract void exec(T player, V module, IMessage req) throws Exception;

}

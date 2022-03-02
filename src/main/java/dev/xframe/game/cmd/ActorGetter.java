package dev.xframe.game.cmd;

import dev.xframe.game.action.Action;
import dev.xframe.game.action.Actor;
import dev.xframe.game.player.Player;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.utils.XGeneric;

import java.util.Optional;

/**
 *  Actor getter for Cmd To Action via Class extends Action
 */
public interface ActorGetter {

    /**
     * @param cls extends Action
     */
    default void setup(Class<?> cls) {}

    Actor get(Player player, Object msg);

    ActorGetter Default = (p, m) -> p;

    static Class<?> getMsgCls(Class<?> cls) {
        return XGeneric.parse(cls, Action.class).getByIndex(1);
    }
    static ActorGetter from(Class<?> cls) {//cls extends Action
        return Optional.ofNullable(cls.getAnnotation(CmdAction.class)).map(CmdAction::value).map(c->{
            ActorGetter getter = BeanHelper.inject(c);
            getter.setup(cls);
            return getter;
        }).orElse(ActorGetter.Default);
    }
}

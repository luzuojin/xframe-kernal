package dev.xframe.game.cmd;

import dev.xframe.game.action.ActionFactory;
import dev.xframe.game.action.ActionTask;
import dev.xframe.game.action.Actions;
import dev.xframe.game.action.Actor;
import dev.xframe.game.player.Player;

public final class ActionCmd<T extends Player, M> extends DirectCmd<T, M>  {

    final Class<?> cls;
    final ActionFactory fac;
    final ActorGetter acg;

    public ActionCmd(Class<?> cls) {
        this.cls = cls;
        this.fac = Actions.getFactoryByCls(cls);
        this.acg = ActorGetter.from(cls);
    }

    @Override
    protected Class<?> getExplicitCls() {
        return ActorGetter.getMsgCls(cls);
    }

    @Override
    public void exec(T player, M msg) throws Exception {
        Actor actor = acg.get(player, msg);
        //run action (looped)
        ActionTask.of(fac.make(actor), actor, msg).checkin();
    }

    @Override
    public Class<?> getClazz() {
        return cls;
    }

}

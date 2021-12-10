package dev.xframe.game.cmd;

import dev.xframe.game.action.Action;
import dev.xframe.game.action.Actions;
import dev.xframe.game.action.ActionFactory;
import dev.xframe.game.action.ActionTask;
import dev.xframe.game.player.Player;
import dev.xframe.utils.XGeneric;

public final class ActionCmd<T extends Player, M> extends DirectCmd<T, M>  {
    
    final Class<?> cls;
    final ActionFactory fac;
    
    public ActionCmd(Class<?> cls) {
        this.cls = cls;
        this.fac = Actions.getFactoryByCls(cls);
    }
    
    @Override
    protected Class<?> getExplicitCls() {
        return XGeneric.parse(cls, Action.class).getByIndex(1);
    }
    
    @Override
    public void exec(T player, M msg) throws Exception {
        //run action (looped)
        ActionTask.of(fac.make(player), player, msg).checkin();
    }
    
    @Override
    public Class<?> getClazz() {
        return cls;
    }

}

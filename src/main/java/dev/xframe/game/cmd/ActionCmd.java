package dev.xframe.game.cmd;

import dev.xframe.game.action.Action;
import dev.xframe.game.action.ActionBuilder;
import dev.xframe.game.action.ActionTask;
import dev.xframe.game.player.Player;
import dev.xframe.utils.XGeneric;

public final class ActionCmd<T extends Player, M> extends DirectCmd<T, M>  {
    
    final Class<?> cls;
    final ActionBuilder fac;
    
    public ActionCmd(Class<?> cls) {
        this.cls = cls;
        this.fac = ActionBuilder.of(cls, false);
    }
    
    @Override
    protected Class<?> getExplicitCls() {
        return XGeneric.parse(cls, Action.class).getByIndex(1);
    }
    
    @Override
    public void exec(T player, M msg) throws Exception {
        Action<T, M> action = fac.build(player);
        //run action (looped)
        ActionTask.trusted(action, player, msg).checkin();
    }
    
    @Override
    public Class<?> getClazz() {
        return cls;
    }

}

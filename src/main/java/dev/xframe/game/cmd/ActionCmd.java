package dev.xframe.game.cmd;

import dev.xframe.game.action.Action;
import dev.xframe.game.action.ActionBuilder;
import dev.xframe.game.player.Player;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XCaught;

public final class ActionCmd<T extends Player> extends DirectCmd<T>  {

    final Class<?> actionCls;
    final ActionBuilder builder;
    final LiteParser msgParser;
    
    public ActionCmd(Class<?> cls) {
        try {
            BeanHelper.inject(this);
            this.actionCls = cls;
            this.builder = ActionBuilder.of(cls);
            this.msgParser = new LiteParser(cls, Action.class, "M");
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }
    
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exec(T player, IMessage req) throws Exception {
        Action<T, Object> action = builder.build(player);
        //transfer msg
        Object msg = msgParser.parse(req.getBody());
        //run action (looped)
        player.accept(action, msg);
    }
    
    @Override
    public Class<?> getClazz() {
        return actionCls;
    }

}

package dev.xframe.game.cmd;

import java.util.function.Supplier;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.inject.beans.Injector;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XLambda;

public final class ActionCmd<T extends Player> extends LoopedCmd<T>  {

    @Inject
    private ModularAdapter adapter;
    
    final Class<?> clazz;
    final Supplier<?> factory;
    final Injector injector;
    final MTypedLoader mLoader;
    final LiteParser msgParser;
    
    public ActionCmd(Class<?> clazz) {
        try {
            BeanHelper.inject(this);
            this.clazz = clazz;
            this.factory = XLambda.createByConstructor(clazz);
            this.mLoader = ModularAction.class.isAssignableFrom(clazz) ? adapter.getTypedLoader(ModularAction.getModuleType(clazz)) : null;
            this.injector = adapter.newInjector(clazz);
            this.msgParser = new LiteParser(clazz, Action.class, "M");
        } catch (Throwable e) {
            throw XCaught.throwException(e);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exec(T player, IMessage req) throws Exception {
        Action<T, Object> action = (Action<T, Object>) factory.get();
        //inject
        adapter.runInject(injector, action, player);
        //setup module loader if require
        if(action instanceof ModularAction) {
            ((ModularAction) action).mTypedLoader = mLoader;
        }
        //transfer msg
        Object msg = msgParser.parse(req.getBody());
        //run action
        action.exec(player, msg);
    }
    
    @Override
    public Class<?> getClazz() {
        return clazz;
    }

}

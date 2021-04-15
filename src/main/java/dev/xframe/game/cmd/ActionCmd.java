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
    final Injector injector;
    final Supplier<?> getter;
    final MTypedLoader loader;
    final LiteParser liteParser;
    
    public ActionCmd(Class<?> clazz) {
        try {
            BeanHelper.inject(this);
            this.clazz = clazz;
            this.getter = XLambda.createByConstructor(clazz);
            this.loader = adapter.getTypedLoader(Action.getModuleType(clazz));
            this.injector = adapter.newInjector(clazz);
            this.liteParser = LiteAction.class.isAssignableFrom(clazz) ? new LiteParser(clazz, LiteAction.class, "L") : null;
        } catch (Throwable e) {
            throw XCaught.wrapException(clazz.getName(), e);
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exec(T player, IMessage req) throws Exception {
        Action<T, ?> action = (Action<T, ?>) getter.get();
        adapter.runInject(injector, action, player);
        if(action instanceof LiteAction) {
        	((LiteAction) action).parser = liteParser;
        }
        action.exec(player, loader.load(player), req);
    }
    
    @Override
    public Class<?> getClazz() {
        return clazz;
    }

}

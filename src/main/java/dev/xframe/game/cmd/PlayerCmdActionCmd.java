package dev.xframe.game.cmd;

import java.util.function.Supplier;

import dev.xframe.game.player.ModularPlayer;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.Injector;
import dev.xframe.module.ModularConext;
import dev.xframe.module.ModularInjection;
import dev.xframe.module.ModuleTypeLoader;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.LiteParser;
import dev.xframe.utils.XCaught;
import dev.xframe.utils.XLambda;

public final class PlayerCmdActionCmd<T extends ModularPlayer> extends PlayerCommand<T>  {

    final Class<?> clazz;
    final Injector injector;
    final Supplier<?> getter;
    final ModuleTypeLoader loader;
    final LiteParser liteParser;
    final PlayerCmdInvoker<T> invoker;
    
    @SuppressWarnings("unchecked")
    public PlayerCmdActionCmd(Class<?> clazz) {
        try {
            this.clazz = clazz;
            this.getter = XLambda.createByConstructor(clazz);
            this.loader = ModularConext.getLoader(PlayerCmdAction.getModuleType(clazz));
            this.injector = ModularInjection.build(clazz);
            this.liteParser = PlayerCmdLiteAction.class.isAssignableFrom(clazz) ? new LiteParser(clazz, PlayerCmdLiteAction.class) : null;
            this.invoker = ApplicationContext.fetchBean(PlayerCmdInvoker.class);
        } catch (Throwable e) {
            throw XCaught.wrapException(clazz.getName(), e);
        }
    }
    
    @Override
    protected void execute0(T player, IMessage req) throws Exception {
        new PlayerCmdInvokeAction<>(invoker, this, player, req, player.loop()).checkin();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void exec(T player, IMessage req) throws Exception {
        PlayerCmdAction<T, ?> action = (PlayerCmdAction<T, ?>) getter.get();
        ModularInjection.inject(action, injector, player);
        if(action instanceof PlayerCmdLiteAction) {
        	((PlayerCmdLiteAction) action).parser = liteParser;
        }
        action.exec(player, loader.load(player), req);
    }
    
    @Override
    public Class<?> getClazz() {
        return clazz;
    }

}

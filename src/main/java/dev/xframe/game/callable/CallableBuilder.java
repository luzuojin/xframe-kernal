package dev.xframe.game.callable;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.beans.Injector;

@Prototype
class CallableBuilder {
    
    @Inject
    private ModularAdapter adapter;
    
    private Injector injector;
    
    private MTypedLoader loader;
    
    public CallableBuilder setup(Class<?> type, Class<?> moduleType) {
        injector = adapter.newInjector(type);
        if(moduleType != null) {
            loader = adapter.getTypedLoader(moduleType);
        }
        return this;
    }
    
    public <T> T apply(Player player, Object bean) {
        adapter.runInject(injector, bean, player);;
        return loader == null ? null : loader.load(player);
    }

}

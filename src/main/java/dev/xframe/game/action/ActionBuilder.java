package dev.xframe.game.action;

import java.util.HashMap;
import java.util.Map;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.beans.Injector;
import dev.xframe.utils.XFactory;

@Prototype
public class ActionBuilder {
    
    private XFactory<?> fac;
    private Injector injector;
    //optional
    private MTypedLoader mTyped;
    @Inject
    private ModularAdapter adapter;
    
    private ActionBuilder(Class<?> cls) {
        this.fac = XFactory.of(cls);
        this.injector = adapter.newInjector(cls);
        this.mTyped = IModularAction.class.isAssignableFrom(cls) ? adapter.getTypedLoader(IModularAction.getModuleType(cls)) : null;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Player> Action<T, Object> build(Player p) {
        return (Action<T, Object>) makeCompelte(fac.get(), p);
    }

    @SuppressWarnings("unchecked")
    public <T extends Player> Object makeCompelte(Object a, Player p) {
        if(a instanceof IModularAction) {
            ((IModularAction<?>) a).mTypedLoader = mTyped;
        }
        adapter.runInject(injector, a, p);
        return a;
    }
    
    private static Map<Class<?>, ActionBuilder> cached = new HashMap<>();
    public static ActionBuilder of(Class<?> cls) {
        ActionBuilder ab = cached.get(cls);
        if(ab == null) {
            ab = new ActionBuilder(cls);
            cached.put(cls, ab);
        }
        return ab;
    }
    
}

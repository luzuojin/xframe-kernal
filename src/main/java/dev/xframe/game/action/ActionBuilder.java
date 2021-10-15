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
    
    private boolean onlyCompletion;
    
    private XFactory<?> factory;
    
    private Injector injector;
    //optional
    private MTypedLoader mTyped;
    @Inject
    private ModularAdapter adapter;
    
    private ActionBuilder(Class<?> cls, boolean onlyCompletion) {
        this.onlyCompletion = onlyCompletion;
        this.factory = onlyCompletion ? null : XFactory.of(cls);
        this.injector = adapter.newInjector(cls);
        this.mTyped = IModularAction.class.isAssignableFrom(cls) ? adapter.getTypedLoader(IModularAction.getModuleType(cls)) : null;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Player, M> Action<T, M> build(Player p) {
        if(onlyCompletion) {
            throw new IllegalArgumentException("this ActionBuilder is only for Instance completion");
        }
        return (Action<T, M>) makeCompelte(factory.get(), p);
    }

    @SuppressWarnings("unchecked")
    public Object makeCompelte(Object a, Player p) {
        if(a instanceof IModularAction) {
            ((IModularAction<?>) a).mTyped = mTyped;
        }
        adapter.runInject(injector, a, p);
        return a;
    }
    
    public static void ensureCompleted(Object a, Player p) {
        if(a instanceof IModularAction
                && ((IModularAction<?>) a).mTyped != null) {
            //completed
            return;
        }
        of(a.getClass(), true).makeCompelte(a, p);
    }
    
    /**---caches---*/
    private final static Map<Class<?>, ActionBuilder> cached = new HashMap<>();
    
    /**
     * @param cls
     * @param onlyCompletion
     * @return
     */
    public static ActionBuilder of(Class<?> cls, boolean onlyCompletion) {
        ActionBuilder ab = cached.get(cls);
        if(ab == null) {
            ab = new ActionBuilder(cls, onlyCompletion);
            cached.put(cls, ab);
        }
        return ab;
    }
    
}

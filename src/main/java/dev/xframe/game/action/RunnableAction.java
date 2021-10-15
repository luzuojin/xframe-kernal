package dev.xframe.game.action;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.utils.XGeneric;

/**
 * Void Action 
 * @author luzj
 * @param <T>
 */
public interface RunnableAction<T extends Player> extends Action<T, Void>{
    
    @Override
    default void exec(T player, Void msg) throws Exception {
        exec0(player);
    }
    
    public void exec0(T player) throws Exception;
    
    /**
     * for implemention
     * @author luzj
     * @param <T>
     * @param <V>
     */
    public static abstract class Modularized<T extends Player, V> extends IModularAction<V> implements RunnableAction<T> {
        @Override
        public final void exec0(T player) throws Exception {
            exec0(player, mTyped.load(player));
        }
        public abstract void exec0(T player, V module) throws Exception;
    }

    public static <T extends Player> RunnableAction<T> of(Consumer<T> func) {
        return player -> func.accept(player);
    }
    public static <T extends Player, V> RunnableAction<T> of(BiConsumer<T, V> func) {
        return of(func, getMTypeLoader(func));
    }
    
    public static <T extends Player> Supplier<RunnableAction<T>> factory(Consumer<T> func) {
        return () -> of(func);
    }
    public static <T extends Player, V> Supplier<RunnableAction<T>> factory(BiConsumer<T, V> func) {
        final MTypedLoader mTyped = getMTypeLoader(func);
        return () -> of(func, mTyped);
    }
    
    static <T extends Player, V> RunnableAction<T> of(BiConsumer<T, V> func, MTypedLoader mTyped) {
        return player -> func.accept(player, mTyped.load(player));
    }
    
    static <T extends Player, V> MTypedLoader getMTypeLoader(BiConsumer<T, V> func) {
        final Class<?> mType = XGeneric.parse(func.getClass(), BiConsumer.class).getByIndex(1);
        final ModularAdapter adapter = ApplicationContext.getBean(ModularAdapter.class);
        return adapter.getTypedLoader(mType);
    }

}

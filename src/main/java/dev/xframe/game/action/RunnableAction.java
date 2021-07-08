package dev.xframe.game.action;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.utils.XGeneric;

public interface RunnableAction<T extends Player>{
    
    void exec(T player) throws Exception;
    
    /**
     * for implemention
     * @author luzj
     * @param <T>
     * @param <V>
     */
    public static abstract class Modular<T extends Player, V> extends IModularAction<V> implements RunnableAction<T> {
        @Override
        public final void exec(T player) throws Exception {
            ActionBuilder.of(getClass()).makeCompelte(this, player);
            exec(player, mTypedLoader.load(player));
        }
        public abstract void exec(T player, V module) throws Exception;
    }

    static class ModularFunc<T extends Player> implements RunnableAction<T> {
        final BiConsumer<T, ?> func;
        final MTypedLoader typed;
        public ModularFunc(final BiConsumer<T, ?> func, final MTypedLoader mTypedLoader) {
            this.func = func;
            this.typed = mTypedLoader;
        }
        @Override
        public void exec(T player) throws Exception {
            func.accept(player, typed.load(player));
        }
    }
    
    static class SimpleFunc<T extends Player> implements RunnableAction<T> {
        final Consumer<T> func;
        public SimpleFunc(final Consumer<T> func) {
            this.func = func;
        }
        @Override
        public void exec(T player) throws Exception {
            func.accept(player);
        }
    }
    
    public static <T extends Player, V> RunnableAction<T> of(BiConsumer<T, V> func) {
        return new ModularFunc<>(func, getMTypeLoader(func));
    }
    public static <T extends Player> RunnableAction<T> of(Consumer<T> func) {
        return new SimpleFunc<>(func);
    }
    
    public static <T extends Player, V> Supplier<RunnableAction<T>> factory(BiConsumer<T, V> func) {
        final MTypedLoader loader = getMTypeLoader(func);
        return () -> new ModularFunc<>(func, loader);
    }
    public static <T extends Player> Supplier<RunnableAction<T>> factory(Consumer<T> func) {
        return () -> new SimpleFunc<>(func);
    }
    
    static <T extends Player, V> MTypedLoader getMTypeLoader(BiConsumer<T, V> func) {
        final Class<?> mType = XGeneric.parse(func.getClass(), BiConsumer.class).getByIndex(1);
        final ModularAdapter adapter = ApplicationContext.getBean(ModularAdapter.class);
        return adapter.getTypedLoader(mType);
    }

}

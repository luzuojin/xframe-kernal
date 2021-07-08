package dev.xframe.game.cmd;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.ApplicationContext;
import dev.xframe.utils.XGeneric;

public abstract class RunnableAction<T extends Player> implements Action<T, EmptyMsg> {
    
    @Override
    public final void exec(T player, EmptyMsg msg) throws Exception {
        exec(player);
    }

    public abstract void exec(T player) throws Exception;

    static class WithModule<T extends Player> extends RunnableAction<T> {
        final BiConsumer<T, ?> func;
        final MTypedLoader loader;
        public WithModule(final BiConsumer<T, ?> func, final MTypedLoader loader) {
            this.func = func;
            this.loader = loader;
        }
        @Override
        public void exec(T player) throws Exception {
            func.accept(player, loader.load(player));
        }
    }
    static class WithoutModule<T extends Player> extends RunnableAction<T> {
        final Consumer<T> func;
        public WithoutModule(final Consumer<T> func) {
            this.func = func;
        }
        @Override
        public void exec(T player) throws Exception {
            func.accept(player);
        }
    }
    
    public static <T extends Player, V> RunnableAction<T> of(BiConsumer<T, V> func) {
        return new WithModule<>(func, getMTypeLoader(func));
    }
    public static <T extends Player> RunnableAction<T> of(Consumer<T> func) {
        return new WithoutModule<>(func);
    }
    
    public static <T extends Player, V> Supplier<RunnableAction<T>> factory(BiConsumer<T, V> func) {
        final MTypedLoader loader = getMTypeLoader(func);
        return () -> new WithModule<>(func, loader);
    }
    public static <T extends Player> Supplier<RunnableAction<T>> factory(Consumer<T> func) {
        return () -> new WithoutModule<>(func);
    }
    
    private static <T extends Player, V> MTypedLoader getMTypeLoader(BiConsumer<T, V> func) {
        final Class<?> mType = XGeneric.parse(func.getClass(), BiConsumer.class).getByIndex(1);
        final ModularAdapter adapter = ApplicationContext.getBean(ModularAdapter.class);
        final MTypedLoader loader = adapter.getTypedLoader(mType);
        return loader;
    }
    

}

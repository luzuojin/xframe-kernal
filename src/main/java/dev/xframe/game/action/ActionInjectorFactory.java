package dev.xframe.game.action;

import dev.xframe.game.player.MTypedLoader;
import dev.xframe.game.player.ModularAdapter;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Composite;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Ordered;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.inject.beans.Injector;
import dev.xframe.utils.XGeneric;

@Composite
public interface ActionInjectorFactory<T extends Actor> {

    default ActionInjector make(Class<?> cls) {
        Class<?> aifActor = XGeneric.parse(this.getClass(), ActionInjectorFactory.class).getOnlyType();
        Class<?> actActor = XGeneric.parse(cls, Action.class).getByIndex(0);
        return aifActor.isAssignableFrom(actActor) ? make0(cls) : null;
    }

    ActionInjector make0(Class<?> cls);

    @Bean
    @Ordered(Integer.MIN_VALUE)//set as last composited element
    class Defaults implements ActionInjectorFactory<Actor> {
        @Override
        public ActionInjector make0(Class<?> cls) {
            final Injector injector = BeanHelper.makeInjector(cls);
            return (action, actor) -> {
                BeanHelper.inject(action, injector);
            };
        }
    }

    @Bean
    class Modular implements ActionInjectorFactory<Player> {
        @Inject
        private ModularAdapter adapter;
        @Override
        public ActionInjector make0(Class<?> cls) {
            final Injector injector = adapter.newInjector(cls);
            if(ModularAction.class.isAssignableFrom(cls)) {
                final Class<?> mCls = XGeneric.parse(cls, ModularAction.class).getByIndex(1);
                final MTypedLoader mtLoader = adapter.getTypedLoader(mCls);
                return (action, actor) -> {
                    ((ModularAction<?, ?, ?>) action).mTyped = mtLoader;
                    adapter.runInject(injector, action, (Player) actor);
                };
            }
            return (action, actor) -> {
                adapter.runInject(injector, action, (Player) actor);
            };
        }
    }

}

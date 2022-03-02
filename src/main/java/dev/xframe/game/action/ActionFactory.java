package dev.xframe.game.action;

import dev.xframe.utils.XFactory;

public class ActionFactory {

    Class<?> cls;
    XFactory fac;
    ActionInjector injector;

    public ActionFactory(Class<?> cls, ActionInjector injector) {
        this.cls = cls;
        this.fac = XFactory.of(cls);
        this.injector = injector;
    }

    public <T extends Actor, M> Action<T, M> make(Actor actor) {
        Action<T, M> action = (Action<T, M>) fac.get();
        injector.inject(action, actor);
        return action;
    }

}

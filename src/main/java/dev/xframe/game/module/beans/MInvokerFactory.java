package dev.xframe.game.module.beans;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import dev.xframe.inject.beans.BeanIndexing;

@Bean
@Providable
public class MInvokerFactory {
    
    public ModularInvoker makeInvoker(Class<?> moduleCls, BeanIndexing indexing) {
        return MInvokerBuilder.build(moduleCls, indexing);
    }

}

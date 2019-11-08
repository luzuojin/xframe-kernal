package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.module.Component;
import dev.xframe.module.ModularInject;

@Component
public class TComponent implements Loadable {
    
    @Inject
    private TestExecution testExecution;
    @ModularInject
    private TSharablePlayer player;
    @ModularInject
    private TDepInventory dep;

    @Override
    public void load() {
        Assert.assertNotNull(player);
        Assert.assertNotNull(dep);
        testExecution.executing(TComponent.class);
    }
    
}

package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Loadable;
import dev.xframe.modular.Component;
import dev.xframe.modular.ModularInject;

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

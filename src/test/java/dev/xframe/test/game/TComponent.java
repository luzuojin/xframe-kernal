package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.game.module.ModularComponent;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;

@ModularComponent
public class TComponent implements Loadable {
    
    @Inject
    private TestExecution testExecution;
    @Inject
    private TSharablePlayer player;
    @Inject
    private TDepInventory dep;

    @Override
    public void load() {
        Assert.assertNotNull(player);
        Assert.assertNotNull(dep);
        testExecution.executing(TComponent.class);
    }
    
}

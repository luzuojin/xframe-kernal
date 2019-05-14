package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Loadable;
import dev.xframe.modular.ModularDependence;
import dev.xframe.modular.ModularInject;
import dev.xframe.modular.Module;
import dev.xframe.modular.ModuleType;

@ModularDependence(TSharablePlayer.class)
@Module(ModuleType.TRANSIENT)
public class TDepInventory implements TInventory, TSharableDep, Loadable, TAgent {

    @Inject
    private TestExecution testExecution;
    @ModularInject
    private TPlayer player;
    @ModularInject
    private TSharablePlayer sharablePlayer;
    
    public void load(TPlayer player) {
        //do nothing
    }
    
    public void save() {
        testExecution.executing(TDepInventory.class);
    }
    
    @Override
    public void load() {
        Assert.assertNotNull(this.player);
        Assert.assertNotNull(this.sharablePlayer);
        this.sharablePlayer.dosomething();
        testExecution.executing(TDepInventory.class);
    }

    @Override
    public boolean dox() {
        //do nothing
    	return true;
    }

}

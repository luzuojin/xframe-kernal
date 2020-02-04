package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Dependence;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.module.Module;
import dev.xframe.module.ModuleType;

@Dependence(TSharablePlayer.class)
@Module(ModuleType.TRANSIENT)
public class TDepInventory implements TInventory, TSharableDep, Loadable, TAgent {

    @Inject
    private TestExecution testExecution;
    @Inject
    private TPlayer player;
    @Inject
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

	@Override
	public void dep() {
	}

}

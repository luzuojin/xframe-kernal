package dev.xframe.test.game;

import dev.xframe.event.Subscribe;
import dev.xframe.game.module.Module;
import dev.xframe.game.module.ModuleType;
import dev.xframe.inject.Inject;
import org.junit.Assert;

@Module(ModuleType.RESIDENT)
public class TPlayerInventory implements TInventory, TSharablePlayer, TAgent {
    
	@Inject
    private TRepository repository;
	@Inject
	private TestExecution testExecution;
	@Inject(lazy=true)
	private TSharableDep sharableDep;
	
	public void load(TPlayer player) {
		Assert.assertNotNull(player);
		Assert.assertNotNull(repository);
		testExecution.executing(TPlayerInventory.class);
	}
	
	public void save() {
	    repository.save();
	    testExecution.executing(TPlayerInventory.class);
	}
	
	@Subscribe
	public void onDosomething(TEvent evt) {
	    testExecution.executing(TPlayerInventory.class);
	}
	
    @Override
    public void dosomething() {
        testExecution.executing(TPlayerInventory.class);
    }

    @Override
    public boolean dox() {
        return false;
    }

    @TCallerAnno
    public int callmodule(int x) {
        return (int) Math.pow(x, 2);
    }

}

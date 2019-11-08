package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.event.Subscribe;
import dev.xframe.inject.Inject;
import dev.xframe.module.Module;
import dev.xframe.module.ModuleType;

@Module(ModuleType.RESIDENT)
public class TPlayerInventory implements TInventory, TSharablePlayer {
    
	@Inject
    private TRepository repository;
	@Inject
	private TestExecution testExecution;
	
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

}

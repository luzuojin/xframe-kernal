package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.event.Subscribe;
import dev.xframe.injection.Inject;
import dev.xframe.modular.Module;
import dev.xframe.modular.ModuleType;

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

package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Component;

@Component
public class TComp implements Loadable {
    
    @Inject
    private TRepository repository;
    @Inject
    private TestExecution testExecution;

    @Override
    public void load() {
        Assert.assertNotNull(repository);
        testExecution.executing(TComp.class);
    }

}

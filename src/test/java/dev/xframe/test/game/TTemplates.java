package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Loadable;
import dev.xframe.injection.Templates;

@Templates
public class TTemplates implements Loadable {
    
    @Inject
    private TRepository repository;
    @Inject
    private TestExecution testExecution;

    @Override
    public void load() {
        Assert.assertNotNull(repository);
        testExecution.executing(TTemplates.class);
    }

}

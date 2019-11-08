package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Templates;

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

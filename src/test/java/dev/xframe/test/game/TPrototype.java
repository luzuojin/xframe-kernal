package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Inject;

public class TPrototype extends TPrototypeAbst {
    
    @Inject
    private TComp tComp;
    @Inject
    private TestExecution testExecution;
    
    public void dosomething() {
        super.dosomething();
        Assert.assertNotNull(tComp);
    }
    
}

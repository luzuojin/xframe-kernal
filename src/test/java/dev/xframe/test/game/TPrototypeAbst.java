package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;


@Prototype
public class TPrototypeAbst {
    
    @Inject
    private TRepository tRepository;
    
    public void dosomething() {
        Assert.assertNotNull(tRepository);
    }
    

}

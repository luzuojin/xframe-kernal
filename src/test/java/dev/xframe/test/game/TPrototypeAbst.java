package dev.xframe.test.game;

import org.junit.Assert;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Prototype;


@Prototype
public class TPrototypeAbst {
    
    @Inject
    private TRepository tRepository;
    
    public void dosomething() {
        Assert.assertNotNull(tRepository);
    }
    

}

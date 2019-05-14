package dev.xframe.test.code;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.xframe.injection.Inject;
import dev.xframe.injection.junit.ContextScan;
import dev.xframe.injection.junit.NebulaJunit4ClassRunner;


@RunWith(NebulaJunit4ClassRunner.class)
@ContextScan(includes="dev.xframe.*")
public class FactoryGenTest {
    
    @Inject
    FactoryInterface fi;
    
    @Test
    public void test() {
        Assert.assertTrue(fi.newElement(FactoryEnum.x) instanceof FactoryElementX);
        Assert.assertTrue(fi.newElement(FactoryEnum.y) instanceof FactoryElementY);
    }

}

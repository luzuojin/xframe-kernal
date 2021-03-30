package dev.xframe.test.code;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.xframe.inject.Inject;
import dev.xframe.inject.junit.ContextScan;
import dev.xframe.inject.junit.Junit4ClassRunner;


@RunWith(Junit4ClassRunner.class)
@ContextScan(includes="dev.xframe.test.code.*", excludes="dev.xframe.test.game.*")
public class FactoryGenTest {
    
    @Inject
    FactoryInterface fi;
    
    @Test
    public void test() {
        Assert.assertTrue(fi.newElement(FactoryEnum.x) instanceof FactoryElementX);
        Assert.assertTrue(fi.newElement(FactoryEnum.y) instanceof FactoryElementY);
    }

}

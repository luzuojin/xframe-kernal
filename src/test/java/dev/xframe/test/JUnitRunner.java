package dev.xframe.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.xframe.injection.Inject;
import dev.xframe.injection.junit.ContextScan;
import dev.xframe.injection.junit.Junit4ClassRunner;

@RunWith(Junit4ClassRunner.class)
@ContextScan(includes="dev.xframe.*")
public class JUnitRunner {
    
    @Inject
    private JUnitBean bean;
    
    @Test
    public void test() {
        Assert.assertEquals("JUnitBean", bean.getName());
    }

}

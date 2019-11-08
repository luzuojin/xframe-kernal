package dev.xframe.test.game;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import dev.xframe.inject.Configurator;

@Configurator
public class TestExecution {
    
    Map<Class<?>, Boolean> executed = new HashMap<>();
    
    public void executing(Class<?> clazz) {
        executed.put(clazz, Boolean.TRUE);
    }
    
    public void assertExecuted(Class<?> clazz) {
        Assert.assertNotNull(executed.remove(clazz));
    }

}

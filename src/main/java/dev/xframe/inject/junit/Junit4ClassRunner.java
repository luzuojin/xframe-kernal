package dev.xframe.inject.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.inject.beans.BeanHelper;

public class Junit4ClassRunner extends BlockJUnit4ClassRunner {
    
    private static boolean initialized = false;//only init once
    
    public Junit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.init();
    }

    protected synchronized void init() {
        if(!initialized) {
            Class<?> clazz = getTestClass().getJavaClass();
            ApplicationContext.initialize(getIncludes(clazz), getExcludes(clazz));
            initialized = true;
        }
    }
    
    protected String getIncludes(Class<?> c) {
        return c.isAnnotationPresent(ContextScan.class) ? c.getAnnotation(ContextScan.class).includes() : "*";
    }
    protected String getExcludes(Class<?> c) {
        return c.isAnnotationPresent(ContextScan.class) ? c.getAnnotation(ContextScan.class).excludes() : "";
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        BeanHelper.inject(test);
        return test;
    }

}

package dev.xframe.injection.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import dev.xframe.injection.ApplicationContext;
import dev.xframe.injection.Injection;

public class NebulaJunit4ClassRunner extends BlockJUnit4ClassRunner {
    
    public NebulaJunit4ClassRunner(Class<?> klass) throws InitializationError {
        super(klass);
        this.init();
    }

    protected void init() {
        Class<?> clazz = getTestClass().getJavaClass();
        ApplicationContext.initialize(getIncludes(clazz), getExcludes(clazz));
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
        Injection.inject(test);
        return test;
    }

}

package dev.xframe.module;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.inject.code.SyntheticBuilder;

public class ModularHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(ModularHelper.class);
    
    public static <T> void removeAgent(T agent, T delegate) {
        SyntheticBuilder.remove(agent, delegate);
    }
    
    public static <T> void appendAgent(T agent, T delegate) {
        SyntheticBuilder.append(agent, delegate);
    }
    
    public static <T> void forEachAgent(T agent, Consumer<T> consumer) {
        SyntheticBuilder.forEach(agent, consumer);
    }
    
    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
    public static boolean isModularClass(Class<?> c) {
    	return ModuleContainer.class.isAssignableFrom(c) ||
    			c.isAnnotationPresent(Module.class) ||
    			c.isAnnotationPresent(ModularShare.class) ||
    			c.isAnnotationPresent(ModularAgent.class) ||
    			c.isAnnotationPresent(ModularComponent.class);
    }
    
}

package dev.xframe.module;

import java.util.function.Consumer;

import dev.xframe.inject.beans.BeanIndexes;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.module.beans.ModularIndexes;
import dev.xframe.module.beans.ModuleContainer;

public class ModularHelper {
    
    public static <T> void removeAgent(T agent, T delegate) {
        SyntheticBuilder.remove(agent, delegate);
    }
    
    public static <T> void appendAgent(T agent, T delegate) {
        SyntheticBuilder.append(agent, delegate);
    }
    
    public static <T> void forEachAgent(T agent, Consumer<T> consumer) {
        SyntheticBuilder.forEach(agent, consumer);
    }
    
    public static boolean isModularClass(Class<?> c) {
    	return ModuleContainer.class.isAssignableFrom(c) ||
    			c.isAnnotationPresent(Module.class) ||
    			c.isAnnotationPresent(ModularShare.class) ||
    			c.isAnnotationPresent(ModularAgent.class) ||
    			c.isAnnotationPresent(ModularComponent.class);
    }
    
    public static boolean isResidentModularClass(Class<?> c) {
        return c.isAnnotationPresent(ModularAgent.class)
                || (c.isAnnotationPresent(Module.class) && c.getAnnotation(Module.class).value() == ModuleType.RESIDENT)
                || (c.isAnnotationPresent(ModularComponent.class) && c.getAnnotation(ModularComponent.class).value() == ModuleType.RESIDENT)
                ;
    }
    
    public static boolean isTransientModularClass(Class<?> c) {
        return (c.isAnnotationPresent(Module.class) && c.getAnnotation(Module.class).value() == ModuleType.TRANSIENT)
            || (c.isAnnotationPresent(ModularComponent.class) && c.getAnnotation(ModularComponent.class).value() == ModuleType.TRANSIENT)
               ;
    }
    
    public static boolean isModularIndexes(BeanIndexes indexes) {
    	return indexes.offset() == ModularIndexes.OFFSET;
    }
    
}

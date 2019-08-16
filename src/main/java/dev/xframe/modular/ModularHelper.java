package dev.xframe.modular;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.injection.code.SyntheticBuilder;

public class ModularHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(ModularHelper.class);
    
    public static <T> void appendAgent(T agent, T delegate) {
        SyntheticBuilder.append(agent, delegate);
    }
    
    public static <T> void forEachAgent(T agent, Consumer<T> consumer) {
        SyntheticBuilder.forEach(agent, consumer);
    }
    
    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
}

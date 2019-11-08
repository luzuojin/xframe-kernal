package dev.xframe.test.game;

import java.util.function.Consumer;

import dev.xframe.module.ModularAgent;
import dev.xframe.module.ModularHelper;

@ModularAgent(invokable=false)
public interface TAgent {
    
    public boolean dox();
    
    public default void forEach(Consumer<TAgent> c) {
        ModularHelper.forEachAgent(this, c);
    }

}

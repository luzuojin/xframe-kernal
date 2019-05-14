package dev.xframe.test.game;

import java.util.function.Consumer;

import dev.xframe.modular.ModularAgent;
import dev.xframe.modular.ModularHelper;

@ModularAgent(invokable=false)
public interface TAgent {
    
    public boolean dox();
    
    public default void forEach(Consumer<TAgent> c) {
        ModularHelper.forEachAgent(this, c);
    }

}

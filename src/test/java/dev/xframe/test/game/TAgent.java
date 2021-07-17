package dev.xframe.test.game;

import java.util.function.Consumer;

import dev.xframe.game.module.ModularAgent;
import dev.xframe.game.module.ModularHelper;

@ModularAgent(invokable=false)
public interface TAgent {
    
    public boolean dox();
    
    public default void forEach(Consumer<TAgent> c) {
        ModularHelper.forEachAgent(this, c);
    }

}

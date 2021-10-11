package dev.xframe.game.player;

import dev.xframe.utils.XThreadLocal;

/**
 * hold current thread player
 */
public class PlayerHolder {
    
    static final XThreadLocal<Player> Data = new XThreadLocal<>();
    
    @SuppressWarnings("unchecked")
    public static <T extends Player> T get() {
        return (T) Data.get();
    }
    
    public static void set(Player player) {
        Data.set(player);
    }
    
    public static void unset() {
        Data.remove();
    }

}

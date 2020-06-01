package dev.xframe.game.player;

/**
 * 每一个Module Type对应一个Loader
 * @author luzj
 */
@FunctionalInterface
public interface MTypedLoader {
    
    public <T> T load(Player player);
    
}

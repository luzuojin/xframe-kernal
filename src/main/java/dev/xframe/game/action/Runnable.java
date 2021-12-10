package dev.xframe.game.action;

import dev.xframe.game.player.Player;

@FunctionalInterface
public interface Runnable<T extends Player> {
    void run(T player) throws Exception;
}

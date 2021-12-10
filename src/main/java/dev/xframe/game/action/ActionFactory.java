package dev.xframe.game.action;

import dev.xframe.game.player.Player;

public interface ActionFactory {

    <T extends Player, M> Action<T, M> make(Player player);

}

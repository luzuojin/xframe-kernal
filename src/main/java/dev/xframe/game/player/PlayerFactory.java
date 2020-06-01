package dev.xframe.game.player;

import dev.xframe.action.ActionLoop;

@FunctionalInterface
public interface PlayerFactory {

    Player newPlayer(long playerId, ActionLoop loop);

}

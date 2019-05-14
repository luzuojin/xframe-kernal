package dev.xframe.game.player;

import dev.xframe.action.ActionQueue;

public interface PlayerFactory {

    Player newPlayer(long playerId, ActionQueue queue);

}

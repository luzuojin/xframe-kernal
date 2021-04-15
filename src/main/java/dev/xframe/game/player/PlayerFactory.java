package dev.xframe.game.player;

import dev.xframe.task.TaskLoop;

@FunctionalInterface
public interface PlayerFactory {

    Player newPlayer(long playerId, TaskLoop loop);

}

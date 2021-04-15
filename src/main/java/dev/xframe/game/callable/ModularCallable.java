package dev.xframe.game.callable;

import static dev.xframe.game.callable.CallableInjector.doInjectAndGetModule;

import dev.xframe.game.player.Player;
import dev.xframe.task.RunnableTask;
import dev.xframe.task.TaskLoop;

/**
 * 
 * @author luzj
 *
 * @param <T>
 * @param <V>
 */
public interface ModularCallable<T extends Player, V> extends PlayerCallable<T> {
	
    default void call(T player) {
    	TaskLoop loop = player.loop();
    	if(loop.inLoop()) {
    		exec(player);
    	} else {
    		RunnableTask.of(loop, ()->exec(player)).checkin();
    	}
    }
    
    default void exec(T player) {
        exec(player, doInjectAndGetModule(player, this));
    }

    void exec(T player, V module);

}

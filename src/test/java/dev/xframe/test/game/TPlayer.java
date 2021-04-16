package dev.xframe.test.game;

import dev.xframe.event.EventBus;
import dev.xframe.event.Registrator;
import dev.xframe.event.Subscriber;
import dev.xframe.game.Assemble;
import dev.xframe.game.player.Player;
import dev.xframe.inject.Inject;
import dev.xframe.module.ModuleType;
import dev.xframe.task.ScheduledContext;
import dev.xframe.task.TaskLoop;

@Assemble
public class TPlayer extends Player implements Registrator {
    
    @Inject
    public ScheduledContext scheduledCtx;
    @Inject
    public TSharablePlayer player;
    @Inject
    public TAgent agent;
    @Inject
    public TSharableDep dep;
    
    private EventBus events;

	public TPlayer(long playerId, TaskLoop loop) {
		super(playerId, loop);
		this.events = new EventBus();
	}

    @Override
    public boolean unloadable(ModuleType type) {
        return type == ModuleType.TRANSIENT;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    public void post(Object event) {
        events.post(event);
    }

    @Override
    public void regist(Subscriber subscriber) {
        events.regist(subscriber);
    }

    @Override
    public void unregist(int group) {
        events.unregist(group);
    }

    @Override
    public void unregist(Subscriber subscriber) {
        events.unregist(subscriber);
    }

}

package dev.xframe.game.player;

import dev.xframe.game.action.Action;
import dev.xframe.game.action.Actions;
import dev.xframe.game.action.ActionTask;
import dev.xframe.game.action.Runnable;
import dev.xframe.game.module.ModuleType;
import dev.xframe.game.module.beans.ModuleContainer;
import dev.xframe.task.TaskLoop;

public abstract class Player {

	private TaskLoop loop;
	private long id;
    private int loaded;
    //set by player factory
    ModuleContainer mc;
    
    public Player(long id, TaskLoop loop) {
        this.id = id;
        this.loop = loop;
    }
    
    public long id() {
    	return id;
    }
    public TaskLoop loop() {
        return this.loop;
    }

    public final <T extends Player, M> void accept(M msg) {
        exec(Actions.makeByMsg(this, msg), msg);
    }
    public final <T extends Player> void accept(Runnable<T> runnable) {
        exec(Actions.makeByRunnable(this, runnable), runnable);
    }
    @SuppressWarnings("unchecked")
    private <T extends Player, M> void exec(Action<T, M> action, M msg) {
        final T plr = (T) this;
        if(loop.inLoop()) {
            ActionTask.exec(action, plr, msg);
        } else {
            ActionTask.of(action, plr, msg).checkin();
        }
    }

    public synchronized void load(ModuleType type) {
    	this.mc.loadModules(type);
    	this.loaded |= type.code;
    }
    
    public synchronized void unload(ModuleType type) {
    	this.mc.unloadModules(type);
    	this.loaded &= ~type.code;
    }
    
    public void save() {
		this.mc.saveModules();
    }
    
    public void tick() {
        this.mc.tickModules();
    }
    
    public final boolean isLoaded(ModuleType type) {
        return (loaded & type.code) > 0;
    }
    
    public final void load() {
        this.load(ModuleType.RESIDENT);
    }
    
    public final boolean idle(long lastActiveTime) {
        if(unloadable(ModuleType.TRANSIENT, lastActiveTime)) {
            this.unload(ModuleType.TRANSIENT);
        }
        if(unloadable(ModuleType.RESIDENT, lastActiveTime)) {
            this.unload(ModuleType.RESIDENT);
            return true;
        }
        return false;
    }
    
    private boolean unloadable(ModuleType dataType, long lastActiveTime) {
        return this.unloadable(dataType) && this.isLoaded(dataType) && (System.currentTimeMillis() - lastActiveTime) > dataType.unloadIdleTime;
    }
    
	public boolean unloadable(ModuleType type) {
	    //for implemention, default disable
		return false;
	}

	public boolean isOnline() {
		return this.isLoaded(ModuleType.TRANSIENT);
	}

}

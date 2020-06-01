package dev.xframe.game.player;

import dev.xframe.action.ActionLoop;
import dev.xframe.module.ModuleType;
import dev.xframe.module.beans.ModuleContainer;

public abstract class Player {

	private ActionLoop loop;
	private long playerId;
    private int loaded;
    
    final ModuleContainer mc = new ModuleContainer();
    
    public Player(long playerId, ActionLoop loop) {
        this.playerId = playerId;
        this.loop = loop;
    }
    
    public ActionLoop loop() {
        return this.loop;
    }

    public long getPlayerId() {
        return playerId;
    }
    
    public boolean load(ModuleType type) {
    	this.mc.loadModules(type);
    	this.loaded |= type.code;
        return true;
    }
    
    public boolean unload(ModuleType type) {
    	this.mc.unloadModules(type);
    	this.loaded ^= type.code;
        return true;
    }
    
    public boolean save() {
		this.mc.saveModules();
        return true;
    }
    
    public final boolean isLoaded(ModuleType type) {
        return (loaded & type.code) > 0;
    }
    
    public final boolean load() {
        return this.load(ModuleType.RESIDENT);
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
        return this.unloadable(dataType) && (System.currentTimeMillis() - lastActiveTime) > dataType.unloadIdleTime;
    }
    
	public boolean unloadable(ModuleType type) {
		return type == ModuleType.TRANSIENT;
	}

	public boolean isOnline() {
		return this.isLoaded(ModuleType.TRANSIENT);
	}

}

package dev.xframe.game.player;

import dev.xframe.action.ActionQueue;
import dev.xframe.modular.ModuleContainer;
import dev.xframe.modular.ModuleType;

public abstract class ModularPlayer extends Player implements ModuleContainer {

    private int loaded;
    
    public ModularPlayer(long playerId, ActionQueue queue) {
        super(playerId, queue);
    }
    
    public boolean load(ModuleType type) {
    	this.loaded |= type.code;
        return true;
    }
    
    public boolean unload(ModuleType type) {
    	this.loaded ^= type.code;
        return true;
    }
    
    public final boolean isLoaded(ModuleType type) {
        return (loaded & type.code) > 0;
    }
    
    @Override
    public final boolean load() {
        return this.load(ModuleType.RESIDENT);
    }
    
    @Override
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
    
    @Override
	public boolean unloadable(ModuleType type) {
		return type == ModuleType.TRANSIENT;
	}

	@Override
	public boolean isOnline() {
		return this.isLoaded(ModuleType.TRANSIENT);
	}

	public boolean save() {
        return true;
    }
    
}

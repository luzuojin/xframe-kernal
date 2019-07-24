package dev.xframe.game.player;

import dev.xframe.action.ActionLoop;

/**
 * 玩家数据在baseserver中需要用到的数据的一个子集
 * 可以由GamePlayer实现, 也可以由跨服中其他GamePlayer实现
 * @author luzj
 * 
 */
public abstract class Player {
    
    protected ActionLoop loop;
    protected long playerId;

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
    
    public abstract boolean isOnline();
    
    public boolean load() {
        return true;
    }
    
    public boolean save() {
        return true;
    }
    
    public boolean idle(long activeTime) {
        return true;
    }

}
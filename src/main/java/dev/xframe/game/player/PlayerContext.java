package dev.xframe.game.player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.action.Action;
import dev.xframe.action.ActionExecutor;
import dev.xframe.action.ActionLoop;
import dev.xframe.game.callable.PlayerCallable;


/**
 * 所有玩家数据cache
 * @author luzj
 */
@SuppressWarnings("unchecked")
public class PlayerContext {
    
    private Logger logger = LoggerFactory.getLogger(PlayerContext.class);
    
    private final ActionExecutor executor;
    private final PlayerFactory factory;
    private final PlayerCollection players;
    
    public PlayerContext(ActionExecutor executor, PlayerFactory factory) {
        this.executor = executor;
        this.factory = factory;
        this.players = new PlayerCollection();
    }
    
   
    public <T extends Player> T getPlayerExists(long playerId) {
        if(playerId < 1) {
            logger.error("incorrect player id: [" + playerId + "]");
            return null;
        }
        
        return (T) players.get(playerId);
    }
    
    public <T extends Player> T getPlayer(long playerId) {
        if(playerId < 1) {
            logger.error("incorrect player id: [" + playerId + "]");
            return null;
        }
        
        return (T) players.get(playerId);
    }
    
    public <T extends Player> T getPlayerImmediately(long playerId) {
        if(playerId < 1) {
            logger.error("incorrect player id: [" + playerId + "]");
            return null;
        }
        
        Player tmp = players.get(playerId);
        if(tmp != null) {
            return (T) tmp;
        }
        
        //不存在缓存中, 从DB中load, 仅load共享数据(shareData)
		Player player = factory.newPlayer(playerId, new ActionLoop(executor));
		players.put(playerId, player);//放入缓存中
		
        if(!player.load()) {
            logger.error("用户初始化出错");
            players.remove(playerId);
            return null;
        }
        return (T) player;
    }
    
    public <T extends Player> T getPlayerWithLoad(long playerId) {
        if(playerId < 1) {
            logger.error("incorrect player id: [" + playerId + "]");
            return null;
        }
        
        Player tmp = players.get(playerId);
        if(tmp != null) {
            return (T) tmp;
        }
        
        //不存在缓存中, 从DB中load
        final Player player = factory.newPlayer(playerId, new ActionLoop(executor));
        
        players.put(playerId, player);//放入缓存中
        
        //通过playerTask Load数据
        new Action(player.loop) {
            @Override
            protected void exec() {
                if(!player.load()) {
                    logger.error("用户初始化出错");
                    players.remove(player.getPlayerId());
                }
            }
        }.checkin();
        return (T) player;
    }
    
    public <T extends Player> T getPlayerWithLatch(long playerId, CountDownLatch latch) {
        if(playerId < 1) {
            latch.countDown();
            return null;
        }
        
        Player tmp = players.get(playerId);
        if(tmp != null) {
            latch.countDown();
            return (T) tmp;
        }
        
        //不存在缓存中, 从DB中load
        final Player player = factory.newPlayer(playerId, new ActionLoop(executor));
        
        players.put(playerId, player);//放入缓存中
        
        //通过playerTask Load数据
        new Action(player.loop) {
            @Override
            protected void exec() {
                try {
                    if(!player.load()) {
                        logger.error("用户初始化出错");
                        players.remove(player.getPlayerId());
                    }
                } finally {
                    latch.countDown();
                }
            }
        }.checkin();
        return (T) player;
    }
    
    public boolean exists(long playerId) {
        return players.isExist(playerId);
    }
    
    /**
     * 保存所有用户数据
     */
    public void persistence() {
        if(players == null) {
            return;
        }
        
        List<PlayerData> curPlayers = players.datas();
        for (PlayerData curPlayer : curPlayers) {
            try {
                Player player = curPlayer.getData();
                player.save();
                
                if(!player.isOnline() && player.idle(curPlayer.activeTime)) {
                    removePlayer(player);
                }
            } catch (Exception ex) {
                logger.error("persistence players", ex);
            }
        }
    }
    
    public void clear() {
        this.players.clear();
    }
    
    public int playerCount() {
        return this.players.size();
    }
    
    /**
     * 彻底从内存中释放该玩家的所有数据
     * 是否可以不释放?
     * @param player
     */
    public void removePlayer(Player player) {
        players.remove(player.getPlayerId());
    }

    private static class PlayerCollection {
        ConcurrentHashMap<Long, PlayerData> context = new ConcurrentHashMap<>();
        
        public Player get(long playerId) {
            PlayerData item = (PlayerData) context.get(playerId);
            if(item == null) {
                return null;
            }
            item.setActiveTime(System.currentTimeMillis());
            return item.getData();
        }
        
        public boolean isExist(long playerId) {
            return context.containsKey(playerId);
        }

        public void put(long playerId, Player player) {
            if (!context.containsKey(playerId)) {
                context.put(playerId, new PlayerData(player));
            }
        }

        public void remove(long playerId) {
            context.remove(playerId);
        }

        public void clear() {
            context.clear();
        }

        public List<Player> values() {
            List<Player> infos = new ArrayList<Player>();
            synchronized (context) {
                for (PlayerData item : context.values()) {
                    if ((item == null) || (item.getData() == null)) {
                        continue;
                    }
                    infos.add(item.getData());
                }
            }
            return infos;
        }

        public List<PlayerData> datas() {
            List<PlayerData> infos = new ArrayList<PlayerData>();
            synchronized (context) {
                for (PlayerData item : context.values()) {
                    if (item == null) {
                        continue;
                    }
                    infos.add(item);
                }
            }
            return infos;
        }
        
        public int size() {
            return context.size();
        }
    }

    public static class PlayerData {
        private Player data;
        private long createTime;//创建对像的时间
        private long activeTime;//最后的活跃时间
        public PlayerData(Player player) {
            this.data = player;
            this.createTime = this.activeTime = System.currentTimeMillis();
        }
        public Player getData() {
            return data;
        }
        public long getCreateTime() {
            return createTime;
        }
        public void setActiveTime(long activeTime) {
            this.activeTime = activeTime;
        }
    }

    public <T extends Player> void callPlayer(long id, PlayerCallable<T> callable) {
        Player player = players.get(id);
        if(player != null) {
            execCall(callable, player);
        }
    }
    
    public <T extends Player> void callOnlinePlayers(PlayerCallable<T> callable) {
        List<Player> values = players.values();
        for (Player player : values) {
            if(player.isOnline()){
                execCall(callable, player);
            }
        }
    }

    public <T extends Player> void callAllPlayers(PlayerCallable<T> callable) {
        List<Player> values = players.values();
        for (Player player : values) {
        	execCall(callable, player);
        }
    }
    
	private <T extends Player> void execCall(PlayerCallable<T> callable, Player player) {
		try {
			callable.call((T) player);
		} catch (Throwable e) {
			logger.error("Call player throws: ", e);
		}
	}
    
}

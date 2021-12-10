package dev.xframe.game.player;

import dev.xframe.game.action.Runnable;
import dev.xframe.task.Task;
import dev.xframe.task.TaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;


/**
 * 所有玩家数据cache
 * @author luzj
 */
@SuppressWarnings("unchecked")
public class PlayerContext {
    
    private final Logger logger = LoggerFactory.getLogger(PlayerContext.class);
    
    private final PlayerCollection players;
    
    public PlayerContext(TaskExecutor executor, PlayerFactory factory) {
        this.players = new PlayerCollection(makePlayerDataFunc(executor, factory));
    }

	static Function<Long, PlayerData> makePlayerDataFunc(TaskExecutor executor, PlayerFactory factory) {
		return id->new PlayerData(factory.newPlayer(id, executor.newLoop()));
	}
	
	private void logIncorrent(long id) {
		logger.error("Incorrect player: [{}]", id);
	}
	private Player doLoad(Player player) {
	    try {
	        player.load();
	        return player;
	    } catch (Throwable e) {
	        long id = player.id();
	        players.remove(id);
            logger.error(String.format("Load player [%s] error", id), e);
        }
	    return null;
	}
	
    public <T extends Player> T getPlayerExists(long id) {
        if(id < 1) {
            logIncorrent(id);
            return null;
        }
        return (T) players.get(id);
    }
    
    public <T extends Player> T getPlayer(long playerId) {
        if(playerId < 1) {
            logIncorrent(playerId);
            return null;
        }
        return (T) players.get(playerId);
    }

    public <T extends Player> T getPlayerImmediately(long id) {
        if(id < 1) {
            logIncorrent(id);
            return null;
        }
        return (T) doLoad(players.getOrNew(id));
    }

    public <T extends Player> T getPlayerWithLoad(long id) {
        if(id < 1) {
            logIncorrent(id);
            return null;
        }
        Player player = players.getOrNew(id);
        //通过playerTask Load数据
        new Task(player.loop()) {
            @Override
            protected void exec() {
                doLoad(player);
            }
        }.checkin();
        return (T) player;
    }
    
    public <T extends Player> T getPlayerWithLatch(long id, CountDownLatch latch) {
        if(id < 1) {
            latch.countDown();
            return null;
        }
        Player tmp = players.get(id);
        if(tmp != null) {
            latch.countDown();
            return (T) tmp;
        }
        final Player player = players.getOrNew(id);
        //不存在缓存中, 从DB中load
        //通过player.loop Load数据
        new Task(player.loop()) {
            @Override
            protected void exec() {
                try {
                    doLoad(player);
                } finally {
                    latch.countDown();
                }
            }
        }.checkin();
        return (T) player;
    }
    
    public boolean exists(long id) {
        return players.isExist(id);
    }
    
    /**
     * 保存所有用户数据
     */
    public void persistence() {
        if(players == null) {
            return;
        }
        
        Collection<PlayerData> curPlayers = players.datas();
        for (PlayerData curPlayer : curPlayers) {
            try {
                Player player = curPlayer.getData();
                player.save();
                
                if(!player.isOnline() && player.idle(curPlayer.activeTime)) {
                    removePlayer(player);
                }
            } catch (Exception ex) {
                logger.error("Persistence players", ex);
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
        players.remove(player.id());
    }

    private static class PlayerCollection {
    	Function<Long, PlayerData> factory;
        Map<Long, PlayerData> datas = new ConcurrentHashMap<>();
        public PlayerCollection(Function<Long, PlayerData> factory) {
        	this.factory = factory;
		}
        public Player get(long playerId) {
            PlayerData item = datas.get(playerId);
            if(item == null) {
                return null;
            }
            item.setActiveTime(System.currentTimeMillis());
            return item.getData();
        }
        public Player getOrNew(long playerId) {
        	Player tmp = get(playerId);
            if(tmp != null) {
                return tmp;
            }
            PlayerData pd = datas.computeIfAbsent(playerId, factory);
            return pd.data;
		}

		public boolean isExist(long playerId) {
            return datas.containsKey(playerId);
        }

        public void remove(long playerId) {
            datas.remove(playerId);
        }

        public void clear() {
            datas.clear();
        }

        public Collection<PlayerData> datas() {
            return datas.values();
        }

        public int size() {
            return datas.size();
        }
    }

    private static class PlayerData {
        private Player data;
        private long activeTime;//最后的活跃时间
        public PlayerData(Player player) {
            this.data = player;
            this.activeTime = System.currentTimeMillis();
        }
        public Player getData() {
            return data;
        }
        public void setActiveTime(long activeTime) {
            this.activeTime = activeTime;
        }
    }

    public <T extends Player> void callPlayer(long id, Runnable<T> rAction) {
        T player = (T) players.get(id);
        if(player != null) {
        	execCall(rAction, player);
        }
    }

    public <T extends Player> void callOnlinePlayers(Runnable<T> rAction) {
        for (PlayerData data : players.datas()) {
        	execCall(rAction, data, true);
        }
    }

    public <T extends Player> void callAllPlayers(Runnable<T> rAction) {
        for (PlayerData data : players.datas()) {
        	execCall(rAction, data, false);
        }
    }
    
    private <T extends Player> void execCall(Runnable<T> rAction, PlayerData data, boolean onlineRequired) {
    	T player;
    	if(data != null && (player = (T) data.getData()) != null && (!onlineRequired || player.isOnline())) {
    	    execCall(rAction, player);
    	}
    }
    
	private <T extends Player> void execCall(Runnable<T> rAction, T player) {
		try {
		    player.accept(rAction);
		} catch (Throwable e) {
			logger.error("Call player throws: ", e);
        }
	}
	
}

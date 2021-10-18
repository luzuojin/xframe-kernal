package dev.xframe.net.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.XLogger;
import io.netty.channel.Channel;

/**
 * 
 * session组, 对于发送消息时相当于一个session来用
 * 记录id上次使用过的session 尽量保证同一个id的消息使用同一个session来发送(顺序)
 * @author luzj
 *
 */
public class GroupSession extends Session {
    
    /**
     * wheel
     */
    public static class SimpleChooser implements BiFunction<SessionSet, IMessage, Session> {
        @Override
        public Session apply(SessionSet set, IMessage msg) {
            return set.next();
        }
    }
    
    /**
     * bind session by IMessage.id
     */
    public static class IdBindChooser implements BiFunction<SessionSet, IMessage, Session> {
        private final Map<Long, Integer> mapped = new ConcurrentHashMap<>();
        @Override
        public Session apply(SessionSet set, IMessage msg) {
            long id = msg.getId();
            int index;
            Integer ex = mapped.get(id);//mapped index (仅cache index, 防止Session Close时引用没有及时更新)
            if(ex != null && (index = ex.intValue()) != -1) {
                Session session = set.get(index);
                if(session != null && session.isActive()) {
                    return session;
                }
            }
            //new allocate
            index = set.nextIndex();
            if (index != -1) {
                mapped.put(id, index);
            }
            return set.get(index);
        }
    }
    
    private BiFunction<SessionSet, IMessage, Session> chooser;
    
    private long id;
    private SessionSet set = new SessionSet();
    
    public GroupSession() {
        this(new IdBindChooser());
    }
    public GroupSession(BiFunction<SessionSet, IMessage, Session> chooser) {
        this.chooser = chooser;
    }
    
    @Override
    public long id() {
        return id;
    }
    @Override
    public Session bind(long id) {
        this.id = id;
        return this;
    }
    
    @Override
    public void sendMessage(IMessage message) {
        sendMessage(message, null);;
    }

    @Override
    public void sendMessage(IMessage message, OperationListener opListener) {
        Session s = chooser.apply(set, message);
        if(s == null) {
            XLogger.warn("Session not exists for id[{}] send message[{}]", message.getId(), message.getCode());
            return;
        }
        if(!s.isActive()) {
            XLogger.warn("Session not active for id[{}] send message[{}]", message.getId(), message.getCode());
            return;
        }
        if(opListener != null) {
            s.sendMessage(message, opListener);
        } else {
            s.sendMessage(message);
        }
    }

    @Override
    public void sendMessageAndClose(IMessage message) {
        sendMessage(message);
        close();
    }

    @Override
    public boolean isActive() {
        return set.size() > 0;
    }

    @Override
    public void close() {
        set.close();
    }
    
    @Override
    public Channel channel() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void connect(OperationListener opListener) {
        throw new UnsupportedOperationException("GroupSession don`t support connect!");
    }
    
    public void add(Session s) {
        set.add(s);
    }

    public void remove(Session s) {
        set.remove(s);
    }
    
    public int size() {
        return set.size();
    }
    
    public void forEach(Consumer<Session> c) {
        for (Session s : set) {
            c.accept(s);
        }
    }
    
}

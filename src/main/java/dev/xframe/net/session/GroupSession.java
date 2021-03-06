package dev.xframe.net.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    
    private long id;
    
    private SessionSet set = new SessionSet();
    
    private Map<Long, Session> mappings = new ConcurrentHashMap<>();
    
    @Override
    public long id() {
        return id;
    }

    @Override
    public Session bind(long id) {
        this.id = id;
        return this;
    }

    private Session getSession(long id) {
        if (mappings.containsKey(id)) {
            Session session = mappings.get(id);
            if (session.isActive()) {
                return session;
            }
        }
        Session session = set.get();
        if (session != null) {
            mappings.put(id, session);
        }
        return session;
    }
    
    @Override
    public void sendMessage(IMessage message) {
        sendMessage(message, null);;
    }

    @Override
    public void sendMessage(IMessage message, SendingListener slistener) {
        long id = message.getId();
        Session s = getSession(id);
        if(s == null) {
            XLogger.warn("Session not exists for id[{}] send message[{}]", id, message.getCode());
            return;
        }
        if(!s.isActive()) {
            XLogger.warn("Session not active for id[{}] send message[{}]", id, message.getCode());
            return;
        }
        if(slistener != null) {
            s.sendMessage(message, slistener);
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
    public boolean connect() {
        throw new UnsupportedOperationException();
    }
    
    public void add(Session s) {
        set.add(s);
    }

    public void remove(Session s) {
        set.add(s);
    }
    
    public void forEach(Consumer<Session> c) {
        for (Session s : set) {
            c.accept(s);
        }
    }
    
}

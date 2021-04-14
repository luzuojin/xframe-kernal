package dev.xframe.net.session;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import dev.xframe.inject.Bean;
import dev.xframe.net.codec.IMessage;

@Bean
public class SessionContext {
    
    private ConcurrentHashMap<Long, Session> sessions;
    
    public SessionContext() {
        sessions = new ConcurrentHashMap<Long, Session>();        
    }
    
    public Session getSession(long sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * @param id
     * @param channel
     */
    public Session registSession(long id, Session session) {
        return sessions.put(id, session.bind(id));
    }
    
    /**
     * close and remove by session id
     * @param id
     */
    public boolean closeSession(long id) {
        Session s = sessions.remove(id);
        if(s != null) {
            s.close();
            return true;
        }
        return false;
    }
    /**
     * close and remove with session compare
     * @param session
     */
    public boolean closeSession(Session session) {
        if(sessions.remove(session.id(), session)) {
            session.close();
            return true;
        }
        return false;
    }
    
    public int sessionCount() {
        return this.sessions.size();
    }

    public void sendMessage(long sessionId, IMessage message) {
        Session session = sessions.get(sessionId);
        if(session != null) {
            session.sendMessage(message);
        }
    }
    
    /**
     * send message to all sessions
     * @param message
     */
    public void sendMessage(IMessage message) {
    	for (Session session : new ArrayList<>(sessions.values())) {
    		session.sendMessage(message);
    	}
    }

}

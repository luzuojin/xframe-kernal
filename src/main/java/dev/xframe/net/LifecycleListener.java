package dev.xframe.net;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public interface LifecycleListener {
    
    public void onSessionRegister(Session session);
    
    public void onMessageRecieve(Session session, IMessage message);
    
    public void onMessageSending(Session session, IMessage message);
    
    public void onSessionUnRegister(Session session);
    
    public void onExceptionCaught(Session session, IMessage req, Throwable ex);

    public void onMessageFlushSlow(Session session);

}

package dev.xframe.net;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public interface LifecycleListener {
    
    default void onSessionRegister(Session session) {}
    
    default void onMessageRecieve(Session session, IMessage message) {}
    
    default void onMessageSending(Session session, IMessage message) {}
    
    default void onSessionUnRegister(Session session) {}
    
    default void onExceptionCaught(Session session, IMessage message, Throwable ex) {}

    default void onMessageFlushSlow(Session session) {}

}

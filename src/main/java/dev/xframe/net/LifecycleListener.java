package dev.xframe.net;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public interface LifecycleListener {
    
    default void onSessionConnected(Session session) {}
    
    default void onSessionDisconnected(Session session) {}
    /**
     * Idle:
     *  Client no writes in 1min
     *  Server no  reads in 3min
     */
    default void onSessionIdle(Session session) {};
    
    default void onMessageRecieve(Session session, IMessage message) {}
    
    default void onMessageSending(Session session, IMessage message) {}
    
    /**@see io.netty.channel.WriteBufferWaterMark*/
    default void onMessageFlushSlow(Session session) {}
    
    default void onExceptionCaught(Session session, IMessage message, Throwable ex) {}

}

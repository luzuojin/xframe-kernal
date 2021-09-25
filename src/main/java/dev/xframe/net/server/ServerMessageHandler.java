package dev.xframe.net.server;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetMessageHandler;
import dev.xframe.net.session.Session;
import io.netty.channel.ChannelHandlerContext;

public class ServerMessageHandler extends NetMessageHandler {
    
    protected ServerSessionFactory factory;

    public ServerMessageHandler(ServerSessionFactory factory, LifecycleListener listener, MessageHandler handler) {
        super(listener, handler);
        this.factory = factory;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Session session = factory.newSession(ctx.channel(), listener);
        listener.onSessionConnected(session);
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        listener.onSessionDisconnected(Session.get(ctx));
    }
    
}

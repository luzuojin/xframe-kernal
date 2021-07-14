package dev.xframe.net.server;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetMessageHandler;
import dev.xframe.net.session.Session;
import io.netty.channel.ChannelHandlerContext;

public class ServerMessageHandler extends NetMessageHandler {

    public ServerMessageHandler(LifecycleListener listener, MessageHandler handler) {
        super(listener, handler);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Session session = new ServerSession(ctx.channel(), listener);
        listener.onSessionConnected(session);
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        listener.onSessionDisconnected(Session.get(ctx));
    }
    
}

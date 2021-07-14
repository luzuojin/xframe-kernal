package dev.xframe.net.client;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetMessageHandler;
import dev.xframe.net.session.Session;
import io.netty.channel.ChannelHandlerContext;

public class ClientMessageHandler extends NetMessageHandler {

    public ClientMessageHandler(LifecycleListener listener, MessageHandler hanlder) {
        super(listener, hanlder);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //create session at connect
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        //notify when inactive
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        listener.onSessionConnected(Session.get(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //unregistered may caused by connect failed
        listener.onSessionDisconnected(Session.get(ctx));
    }
    
}

package dev.xframe.net.client;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetMessageHandler;
import io.netty.channel.ChannelHandlerContext;

public class ClientMessageHandler extends NetMessageHandler {

    public ClientMessageHandler(LifecycleListener listener, MessageHandler hanlder) {
        super(listener, hanlder);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    }

}

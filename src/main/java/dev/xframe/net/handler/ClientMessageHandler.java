package dev.xframe.net.handler;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageInterceptor;
import dev.xframe.net.cmd.CommandContext;
import io.netty.channel.ChannelHandlerContext;

public class ClientMessageHandler extends NetMessageHandler {

    public ClientMessageHandler(LifecycleListener listener, CommandContext cmds, MessageInterceptor interceptor) {
        super(listener, cmds, interceptor);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    }

}

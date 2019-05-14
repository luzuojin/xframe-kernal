package dev.xframe.net.handler;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageInterceptor;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.session.Session;
import dev.xframe.net.session.Session4Server;
import io.netty.channel.ChannelHandlerContext;

public class ServerMessageHandler extends NetMessageHandler {

    public ServerMessageHandler(LifecycleListener listener, CommandContext cmds, MessageInterceptor interceptor) {
        super(listener, cmds, interceptor);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Session session = new Session4Server(ctx.channel(), listener);
        listener.onSessionRegister(session);
    }
    
}

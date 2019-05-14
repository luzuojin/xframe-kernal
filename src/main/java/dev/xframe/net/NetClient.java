package dev.xframe.net;

import dev.xframe.net.NetChannelInitializer.ClientInitializer;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.codec.MessageCodecs;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import dev.xframe.net.handler.ClientMessageHandler;
import dev.xframe.net.handler.NetMessageHandler;
import dev.xframe.net.session.Session;
import dev.xframe.net.session.Session4Client;
import dev.xframe.tools.ThreadsFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NetClient {
    
    private EventLoopGroup group;
    
    private Bootstrap bootstrap;
    
    private LifecycleListener listener;
    
    public NetClient(int threads, CommandContext cmdCtx, MessageInterceptor interceptor, LifecycleListener listener, short hearbeat) {
        this(threads, cmdCtx, interceptor, listener, Message.build(hearbeat));
    }
    public NetClient(int threads, CommandContext cmdCtx, MessageInterceptor interceptor, LifecycleListener listener, IMessage hearbeat) {
        this.listener = listener;
        this.bootstrap = new Bootstrap();
        this.group = new NioEventLoopGroup(threads, new ThreadsFactory("netty.client"));
        NetMessageHandler dispatcher = new ClientMessageHandler(listener, cmdCtx, interceptor);
        
        this.bootstrap.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ClientInitializer(dispatcher, new MessageCodecs(), hearbeat));
    }
    
    public Session buildSession(String host, int port, long sessionId) {
        return new Session4Client(listener, bootstrap, host, port).bind(sessionId);
    }
    
    public Session connect(String host, int port, long sessionId) {
        try {
            Session4Client session = new Session4Client(listener, bootstrap, host, port);
            session.bind(sessionId);//? sessionId
            session.syncConnect();
            return session;
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public void stop() {
        group.shutdownGracefully();
    }
    
    public void close(Channel channel) {
        channel.close().awaitUninterruptibly();
    }

    public boolean isShutdown() {
        return group.isShuttingDown();
    }

}

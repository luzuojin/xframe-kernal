package dev.xframe.net;

import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.net.NetChannelInitializer.ClientInitializer;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import dev.xframe.net.codec.MessageCrypt;
import dev.xframe.net.handler.ClientMessageHandler;
import dev.xframe.net.handler.NetMessageHandler;
import dev.xframe.net.session.Session;
import dev.xframe.net.session.Session4Client;
import dev.xframe.utils.ThreadsFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NetClient {

    @Inject
    private CommandContext cmdCtx;
    @Inject
    private LifecycleListener listener;
    @Inject
    private MessageInterceptor interceptor;
    
    private EventLoopGroup group;
    private Bootstrap bootstrap;
    
    private int threads;
    private IMessage heartbeat;
    private MessageCrypt crypt;
    
    public NetClient working(int threads) {
        this.threads = threads;
        return this;
    }
    
    public NetClient heartbeat(int heartbeat) {
        return heartbeat(Message.build(heartbeat));
    }
    
    public NetClient heartbeat(IMessage heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }
    
    public NetClient crypting(MessageCrypt crypt) {
        this.crypt = crypt;
        return this;
    }

    private synchronized NetClient initial() {
        if(this.bootstrap == null) {
            Injection.inject(this);
            
            this.bootstrap = new Bootstrap();
            this.group = new NioEventLoopGroup(threads, new ThreadsFactory("netty.client"));
            NetMessageHandler dispatcher = new ClientMessageHandler(listener, cmdCtx, interceptor);
            
            this.bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ClientInitializer(dispatcher, crypt, heartbeat));
            
        }
        return this;
    }
    
    public Session build(String host, int port, long sessionId) {
        return build0(host, port, sessionId);
    }

    private Session4Client build0(String host, int port, long sessionId) {
        this.initial();
        Session4Client session = new Session4Client(listener, bootstrap, host, port);
        session.bind(sessionId);
        return session;
    }
    
    public Session connect(String host, int port, long sessionId) {
        try {
            return build0(host, port, sessionId).syncConnect();
        } catch (InterruptedException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public void shutdown() {
        if(group != null) group.shutdownGracefully();
    }
    
    public void close(Channel channel) {
        channel.close().awaitUninterruptibly();
    }

}

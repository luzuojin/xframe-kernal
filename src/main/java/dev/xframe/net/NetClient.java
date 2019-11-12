package dev.xframe.net;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Injection;
import dev.xframe.net.client.ClientChannelInitializer;
import dev.xframe.net.client.ClientLifecycleListener;
import dev.xframe.net.client.ClientMessageHandler;
import dev.xframe.net.client.ClientMessageInterceptor;
import dev.xframe.net.client.ClientSession;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import dev.xframe.net.codec.MessageCrypt;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NetClient {

    @Inject
    private CommandContext cmdCtx;
    @Inject
    private ClientLifecycleListener listener;
    @Inject
    private ClientMessageInterceptor interceptor;
    
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
            this.group = new NioEventLoopGroup(threads, new XThreadFactory("netty.client"));
            NetMessageHandler dispatcher = new ClientMessageHandler(listener, cmdCtx, interceptor);
            
            this.bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_SNDBUF, 2048)//系统sockets发送数据buff的大小(k)
            .option(ChannelOption.SO_RCVBUF, 2048)//---接收(k)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())//使用bytebuf池, 默认不使用
            .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)//消息缓冲区
            .handler(new ClientChannelInitializer(dispatcher, crypt, heartbeat));
            
        }
        return this;
    }
    
    public Session build(String host, int port, long sessionId) {
        return build0(host, port, sessionId);
    }

    private ClientSession build0(String host, int port, long sessionId) {
        this.initial();
        ClientSession session = new ClientSession(listener, bootstrap, host, port);
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

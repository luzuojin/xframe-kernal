package dev.xframe.net;

import dev.xframe.inject.Injection;
import dev.xframe.net.client.ClientChannelInitializer;
import dev.xframe.net.client.ClientMessageHandler;
import dev.xframe.net.client.ClientSession;
import dev.xframe.net.codec.MessageCrypt;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XCaught;
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
    
    public static int defaultThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    private EventLoopGroup group;
    private Bootstrap bootstrap;
    
    private int threads = defaultThreads();
    private int heartbeatCode;
    private MessageCrypt crypt;
    private MessageHandler handler;
    private LifecycleListener listener;
    
    public NetClient setListener(LifecycleListener listener) {
        this.listener = listener;
        return this;
    }
    public NetClient setThreads(int threads) {
        this.threads = threads;
        return this;
    }
    public NetClient setHeartbeat(int heartbeatCode) {
        this.heartbeatCode = heartbeatCode;
        return this;
    }
    public NetClient setCrypt(MessageCrypt crypt) {
        this.crypt = crypt;
        return this;
    }
    public NetClient setHandler(MessageHandlerPipeline handler) {
        this.handler = handler;
        return this;
    }

    private synchronized NetClient initial() {
        if(this.bootstrap == null) {
            Injection.inject(this);
            
            this.bootstrap = new Bootstrap();
            this.group = new NioEventLoopGroup(threads, new XThreadFactory("netty.client"));
            NetMessageHandler netHandler = new ClientMessageHandler(listener, handler);
            
            this.bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_SNDBUF, 2048)//系统sockets发送数据buff的大小(k)
            .option(ChannelOption.SO_RCVBUF, 2048)//---接收(k)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())//使用bytebuf池, 默认不使用
            .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)//消息缓冲区
            .handler(new ClientChannelInitializer(netHandler, crypt, heartbeatCode));
            
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
            return XCaught.throwException(e);
        }
    }
    
    public void shutdown() {
        if(group != null) group.shutdownGracefully();
    }
    
    public void close(Channel channel) {
        channel.close().awaitUninterruptibly();
    }

}

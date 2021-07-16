package dev.xframe.net;

import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.net.client.ClientChannelInitializer;
import dev.xframe.net.client.ClientLifecycleListener;
import dev.xframe.net.client.ClientMessageHandler;
import dev.xframe.net.client.ClientSession;
import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XProperties;
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
    private int connectTimeout = XProperties.getAsInt("xframe.client.connect.timeout", 3000);//s
    
    private MessageCodec iCodec;
    private MessageHandler handler;
    private ClientLifecycleListener listener;
    
    public NetClient setListener(ClientLifecycleListener listener) {
        this.listener = listener;
        return this;
    }
    public NetClient setThreads(int threads) {
        this.threads = threads;
        return this;
    }
    public NetClient setCodec(MessageCodec iCodec) {
        this.iCodec = iCodec;
        return this;
    }
    public NetClient setHandler(MessageHandler handler) {
        this.handler = handler;
        return this;
    }
    public NetClient setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    private synchronized NetClient ensureBootstrap() {
        if(this.bootstrap == null) {
            BeanHelper.inject(this);
            
            this.bootstrap = new Bootstrap();
            this.group = new NioEventLoopGroup(threads, new XThreadFactory("client"));
            NetMessageHandler netHandler = new ClientMessageHandler(listener, handler);
            
            this.bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .option(ChannelOption.SO_SNDBUF, 2048)//系统sockets发送数据buff的大小(k)
            .option(ChannelOption.SO_RCVBUF, 2048)//---接收(k)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())//使用bytebuf池, 默认不使用
            .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)//消息缓冲区
            .handler(new ClientChannelInitializer(netHandler, iCodec, listener));
        }
        return this;
    }
    
    public Session create(long id, String host, int port) {
        this.ensureBootstrap();
        return new ClientSession(listener, bootstrap, host, port).bind(id);
    }
    
    public void shutdown() {
        if(group != null) group.shutdownGracefully();
    }
    
    public void close(Channel channel) {
        channel.close().awaitUninterruptibly();
    }

}

package dev.xframe.net;

import dev.xframe.net.client.ClientChannelInitializer;
import dev.xframe.net.client.ClientLifecycleListener;
import dev.xframe.net.client.ClientMessageHandler;
import dev.xframe.net.client.ClientSession;
import dev.xframe.net.client.ClientSessionFactory;
import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XCaught;
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

    private EventLoopGroup workGroup;
    private Bootstrap bootstrap;
    
    private int threads = defaultThreads();
    private int connectTimeout = XProperties.getAsInt("xframe.client.connect.timeout", 3000);//s
    
    private MessageCodec codec;
    private MessageHandler handler;
    private ClientLifecycleListener listener;
    private ClientSessionFactory factory = new ClientSessionFactory();
    
    public NetClient setFactory(ClientSessionFactory factory) {
        this.factory = factory;
        return this;
    }
    public NetClient setListener(ClientLifecycleListener listener) {
        this.listener = listener;
        return this;
    }
    public NetClient setThreads(int threads) {
        this.threads = threads;
        return this;
    }
    public NetClient setCodec(MessageCodec codec) {
        this.codec = codec;
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

    private void initialize() {
        if(this.bootstrap != null) {
            return;
        }
        synchronized (this) {
            if(this.bootstrap == null) {
                this.bootstrap = new Bootstrap();
                this.workGroup = new NioEventLoopGroup(threads, new XThreadFactory("client"));
                NetMessageHandler netHandler = new ClientMessageHandler(listener, handler);
                
                this.bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_SNDBUF, 2048)//系统sockets发送数据buff的大小(k)
                .option(ChannelOption.SO_RCVBUF, 2048)//---接收(k)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
                .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())//使用bytebuf池, 默认不使用
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT)//消息缓冲区
                .handler(new ClientChannelInitializer(netHandler, codec, listener));
            }
        }
    }
    
    public Session create(long id, String host, int port) {
        this.initialize();
        return create0(id, host, port);
    }
    private ClientSession create0(long id, String host, int port) {
        ClientSession cs = factory.newSession(listener, bootstrap, host, port);
        cs.bind(id);
        return cs;
    }
    /**
     * await connected
     * throws exception on failed
     */
    public Session connect(long id, String host, int port) {
        try {
            this.initialize();
            return create0(id, host, port).connect();
        } catch (InterruptedException e) {
            throw XCaught.throwException(e);
        }
    }
    
    public void shutdown() {
        if(workGroup != null) workGroup.shutdownGracefully();
    }
    
    public void close(Channel channel) {
        channel.close().awaitUninterruptibly();
    }

}

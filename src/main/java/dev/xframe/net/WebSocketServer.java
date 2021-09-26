package dev.xframe.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.websocket.WebSocketChannelInitlializer;
import dev.xframe.net.websocket.WebSocketLifecycleListener;
import dev.xframe.net.websocket.WebSocketMessageHandler;
import dev.xframe.utils.XThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketServer {
	
	private static Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
	
	public static int defaultThreads() {
        return Runtime.getRuntime().availableProcessors();
    }
	
	private Channel bossChannel;
	private NioEventLoopGroup bossGroup;
	private NioEventLoopGroup workerGroup;
	
	private int threads = defaultThreads();
	private String host;
	private int port;
	private MessageCodec codec;
	private MessageHandler handler;
	private WebSocketLifecycleListener listener;
	
	public WebSocketServer setThreads(int threads) {
        this.threads = threads;
        return this;
    }
	public WebSocketServer setHost(String host) {
		this.host = host;
		return this;
	}
    public WebSocketServer setPort(int port) {
        this.port = port;
        return this;
    }
    public WebSocketServer setCodec(MessageCodec codec) {
        this.codec = codec;
        return this;
    }
    public WebSocketServer setHandler(MessageHandler handler) {
        this.handler = handler;
        return this;
    }
    public WebSocketServer setListener(WebSocketLifecycleListener listener) {
        this.listener = listener;
        return this;
    }
    
    private String getWSURL() {
		return String.format("ws://%s:%d", host, port);
	}

    public WebSocketServer startup() {
	    bossGroup = new NioEventLoopGroup(1, new XThreadFactory("netty.boss"));
	    workerGroup = new NioEventLoopGroup(threads, new XThreadFactory("netty.worker"));
	    WebSocketMessageHandler wsHandler = new WebSocketMessageHandler(listener, handler, getWSURL());
        
        ServerBootstrap bootstrap =
	            new ServerBootstrap()
    	            .group(bossGroup, workerGroup)
    	            .channel(NioServerSocketChannel.class)
    	            .childHandler(new WebSocketChannelInitlializer(wsHandler, codec, listener))
    	            .childOption(ChannelOption.SO_KEEPALIVE, true)//开启时系统会在连接空闲一定时间后像客户端发送请求确认连接是否有效
    	            .childOption(ChannelOption.TCP_NODELAY, true)//关闭Nagle算法
//    	            .childOption(ChannelOption.SO_LINGER, 0)//连接关闭时,偿试把未发送完成的数据继续发送(等待时间, 如果为0则直接设置连接为CLOSE状态 不进行TIME_WAIT...)
    	            .childOption(ChannelOption.SO_SNDBUF, 4096)//系统sockets发送数据buff的大小(k)
    	            .childOption(ChannelOption.SO_RCVBUF, 2048)//---接收(k)
    	            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
    	            .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())//使用bytebuf池, 默认不使用
    	            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(2048*1024, 4096*1024))//消息缓冲区
    	            .option(ChannelOption.SO_REUSEADDR, true)//端口重用,如果开启则在上一个进程未关闭情况下也能正常启动
    	            .option(ChannelOption.SO_BACKLOG, 64);//最大等待连接的connection数量
        
        workerGroup.setIoRatio(100);//优先处理网络任务(IOTask)再处理UserTask
        
	    try {
	        bossChannel = bootstrap.bind(port).sync().channel();
            logger.info("WebSocket listening to port : " + port);
        } catch (InterruptedException e) {
            logger.error("WebSocket start failed ...", e);
        }
	    return this;
	}
	
	public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
	    bossChannel.close().awaitUninterruptibly();
	}

}

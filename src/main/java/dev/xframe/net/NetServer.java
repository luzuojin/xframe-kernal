package dev.xframe.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.server.ServerChannelInitializer;
import dev.xframe.net.server.ServerLifecycleListener;
import dev.xframe.net.server.ServerMessageHandler;
import dev.xframe.net.server.ServerSessionFactory;
import dev.xframe.utils.XThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 网络服务启动
 * @author luzj
 */
public class NetServer {

    private static Logger logger = LoggerFactory.getLogger(NetServer.class);

    public static int defaultThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    private Channel masterChannel;
    private NioEventLoopGroup masterGroup;
    private NioEventLoopGroup workerGroup;

    private int threads = defaultThreads();
    private int port;
    private MessageCodec codec;
    private MessageHandler handler;
    private ServerLifecycleListener listener;
    private ServerSessionFactory factory = new ServerSessionFactory();

    public NetServer setThreads(int threads) {
        this.threads = threads;
        return this;
    }
    public NetServer setPort(int port) {
        this.port = port;
        return this;
    }
    public NetServer setCodec(MessageCodec codec) {
        this.codec = codec;
        return this;
    }
    public NetServer setHandler(MessageHandler handler) {
        this.handler = handler;
        return this;
    }
    public NetServer setListener(ServerLifecycleListener listener) {
        this.listener = listener;
        return this;
    }
    public NetServer setFactory(ServerSessionFactory factory) {
        this.factory = factory;
        return this;
    }

    public NetServer startup() {
        masterGroup = new NioEventLoopGroup(      1, new XThreadFactory("net.master"));
        workerGroup = new NioEventLoopGroup(threads, new XThreadFactory("net"));
        NetMessageHandler netHandler = new ServerMessageHandler(factory, listener, handler);

        ServerBootstrap bootstrap =
                new ServerBootstrap()
                .group(masterGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerChannelInitializer(netHandler, codec, listener))
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
            masterChannel = bootstrap.bind(port).sync().channel();
            logger.info("NetServer listening to port : " + port);
        } catch (InterruptedException e) {
            logger.error("NetServer start failed ...", e);
        }
        return this;
    }

    public void shutdown() {
        masterGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        masterChannel.close().awaitUninterruptibly();
    }

}

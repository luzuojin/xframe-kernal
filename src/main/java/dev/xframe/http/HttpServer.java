package dev.xframe.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.http.service.ServiceContext;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.net.NetServer;
import dev.xframe.utils.ThreadsFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * http server
 * 不支持keepAlive
 * @author luzj
 */
public class HttpServer {
    
    private static Logger logger = LoggerFactory.getLogger(NetServer.class);
    
    public static int defaultThreads() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    @Inject
    private ServiceContext serviceCtx;
    
    private Channel bossChannel;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    
    private int threads = defaultThreads();
    
    private int port;
    
    public HttpServer listening(int port) {
        this.port = port;
        return this;
    }
    
    public HttpServer working(int threads) {
        this.threads = threads;
        return this;
    }
    
    public HttpServer startup() {
        Injection.inject(this);
        
        bossGroup = new NioEventLoopGroup(1, new ThreadsFactory("http.boss"));
        workerGroup = new NioEventLoopGroup(threads, new ThreadsFactory("http.worker"));
        
        ServerBootstrap bootstrap =
                new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpChannelInitializer(serviceCtx))
                    .childOption(ChannelOption.SO_KEEPALIVE, false)//开启时系统会在连接空闲一定时间后像客户端发送请求确认连接是否有效
                    .childOption(ChannelOption.TCP_NODELAY, true)//关闭Nagle算法
//                    .childOption(ChannelOption.SO_LINGER, 0)//连接关闭时,偿试把未发送完成的数据继续发送(等待时间, 如果为0则直接设置连接为CLOSE状态 不进行TIME_WAIT...)
                    .childOption(ChannelOption.SO_SNDBUF, 4086)//系统sockets发送数据buff的大小
                    .childOption(ChannelOption.SO_RCVBUF, 2048)//---接收
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)//使用bytebuf池, 默认不使用
                    .option(ChannelOption.SO_REUSEADDR, true)//端口重用,如果开启则在上一个进程未关闭情况下也能正常启动
                    .option(ChannelOption.SO_BACKLOG, 64);//最大等待连接的connection数量
        
        workerGroup.setIoRatio(100);//优先处理网络任务(IOTask)再处理UserTask
        
        try {
            bossChannel = bootstrap.bind(port).sync().channel();
            logger.info("HttpServer listening to port : " + port);
//            bossChannel.closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("HttpServer start failed ...", e);
        }
        return this;
    }
    
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        bossChannel.close().awaitUninterruptibly();
    }

}

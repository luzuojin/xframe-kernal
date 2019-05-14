package dev.xframe.net;

import java.util.concurrent.TimeUnit;

import dev.xframe.net.codec.MessageCodecs;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public interface NetChannelInitializer {
    
    public static class ServerInitializer extends ChannelInitializer<SocketChannel> {
        private final ChannelHandler handler;
        private final MessageCodecs factory;
        
        public ServerInitializer(ChannelHandler handler, MessageCodecs factory) {
            this.handler = handler;
            this.factory = factory;
        }
        
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", factory.newDecoder());
            pipeline.addLast("encoder", factory.newEncoder());
            pipeline.addLast("idleStateHandler", new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));//300秒不操作将会被断开
            pipeline.addLast("idleHandler", new IdleHandler());
            pipeline.addLast("handler", handler);
        }
    }
    
    static class IdleHandler extends ChannelDuplexHandler {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.READER_IDLE) {//客户端长时间没有操作
                    ctx.close();//关闭连接
                }
                //服务端没有操作 暂不处理
            }
        }
    }
    
    public static class ClientInitializer extends ChannelInitializer<SocketChannel> {
        private final ChannelHandler handler;
        private final MessageCodecs factory;
        private final IMessage heartbeat;
        
        public ClientInitializer(ChannelHandler handler, MessageCodecs factory, IMessage heartbeat) {
            this.handler = handler;
            this.factory = factory;
            this.heartbeat = heartbeat;
        }
        
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", factory.newDecoder());
            pipeline.addLast("encoder", factory.newEncoder());
            pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 60, 0, TimeUnit.SECONDS));//60秒发一次心跳操作
            pipeline.addLast("hearbeatHandler", new HearbeatHandler(heartbeat));
            pipeline.addLast("handler", handler);
        }
    }
    
    static class HearbeatHandler extends ChannelDuplexHandler {
        private final IMessage heartbeat;
        
        public HearbeatHandler(IMessage heartbeat) {
            this.heartbeat = heartbeat;
        }
        
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.WRITER_IDLE) {//客户端长时间没有操作
                    ctx.writeAndFlush(Message.copy(heartbeat));
                }
                //服务端没有操作 暂不处理
            }
        }
    }
   
}

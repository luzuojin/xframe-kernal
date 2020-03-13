package dev.xframe.net.client;

import java.util.concurrent.TimeUnit;

import dev.xframe.net.codec.Message;
import dev.xframe.net.codec.MessageCrypt;
import dev.xframe.net.codec.MessageDecoder;
import dev.xframe.net.codec.MessageEncoder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private final ChannelHandler handler;
    private final MessageCrypt cryption;
    private final int heartbeatCode;

    public ClientChannelInitializer(ChannelHandler handler, MessageCrypt cryption, int heartbeatCode) {
        this.handler = handler;
        this.cryption = cryption;
        this.heartbeatCode = heartbeatCode;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new MessageDecoder(cryption));
        pipeline.addLast("encoder", new MessageEncoder(cryption));
        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 60, 0, TimeUnit.SECONDS));//60秒发一次心跳操作
        pipeline.addLast("hearbeatHandler", new HearbeatHandler(heartbeatCode));
        pipeline.addLast("handler", handler);
    }
    
    static class HearbeatHandler extends ChannelDuplexHandler {
        private final int heartbeatCode;
        
        public HearbeatHandler(int heartbeatCode) {
            this.heartbeatCode = heartbeatCode;
        }
        
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.WRITER_IDLE) {//客户端长时间没有操作
                    ctx.writeAndFlush(Message.build(heartbeatCode));
                }
                //服务端没有操作 暂不处理
            }
        }
    }

}

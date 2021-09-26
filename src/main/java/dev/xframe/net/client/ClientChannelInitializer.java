package dev.xframe.net.client;

import java.util.concurrent.TimeUnit;

import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.codec.NetMessageDecoder;
import dev.xframe.net.codec.NetMessageEncoder;
import dev.xframe.net.session.Session;
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
    private final MessageCodec codec;
    private final ClientLifecycleListener listener;

    public ClientChannelInitializer(ChannelHandler handler, MessageCodec codec, ClientLifecycleListener listener) {
        this.handler = handler;
        this.codec = codec;
        this.listener = listener;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new NetMessageDecoder(codec));
        pipeline.addLast("encoder", new NetMessageEncoder(codec));
        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 60, 0, TimeUnit.SECONDS));//60秒发一次心跳操作
        pipeline.addLast("idleHandler", new IdleHandler());
        pipeline.addLast("handler", handler);
    }
    
    class IdleHandler extends ChannelDuplexHandler {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.WRITER_IDLE) {//客户端长时间没有操作
                    listener.onSessionIdle(Session.get(ctx));
                }
                //服务端没有操作 暂不处理
            }
        }
    }

}

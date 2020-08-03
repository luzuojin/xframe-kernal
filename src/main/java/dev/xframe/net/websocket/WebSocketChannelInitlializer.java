package dev.xframe.net.websocket;

import java.util.concurrent.TimeUnit;

import dev.xframe.net.codec.MessageCodec;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class WebSocketChannelInitlializer extends ChannelInitializer<SocketChannel> {
    
    private final ChannelHandler handler;
    private final MessageCodec iCodec;

    public WebSocketChannelInitlializer(ChannelHandler handler, MessageCodec iCodec) {
        this.handler = handler;
        this.iCodec = iCodec;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("httpcodec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("httpchunked", new ChunkedWriteHandler());
        pipeline.addLast("messageCodec", new WebSocketMessageCodec(iCodec));
        pipeline.addLast("idleStateHandler", new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));//300秒不操作将会被断开
        pipeline.addLast("idleHandler", new IdleHandler());
        pipeline.addLast("handler", handler);
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

}

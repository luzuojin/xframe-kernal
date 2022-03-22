package dev.xframe.http;

import dev.xframe.http.service.ServiceHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.InetSocketAddress;

/**
 * 
 * Dispatch http service methods
 * @author luzj
 *
 */
public class HttpMessageHandler extends ChannelInboundHandlerAdapter {

    private final ServiceHandler handler;

    public HttpMessageHandler(ServiceHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        assert msg instanceof FullHttpRequest;
        FullHttpRequest fhr = (FullHttpRequest) msg;
        Request req = new Request(((InetSocketAddress)(ctx.channel().remoteAddress())).getAddress(), fhr);
        try {
            if(req.isSucc()) {
                handler.exec(ctx, req);
            } else {
                Response.BAD_REQUEST.getWriter().writeTo(ctx, req);
            }
        } finally {
            fhr.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

}

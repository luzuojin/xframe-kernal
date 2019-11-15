package dev.xframe.http;

import java.net.InetSocketAddress;

import dev.xframe.http.response.Responses;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.http.service.ServiceInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * 
 * Dispatch http service methods
 * @author luzj
 *
 */
public class HttpMessageHandler extends ChannelInboundHandlerAdapter {
    
    private final ServiceContext ctx;
    
    public HttpMessageHandler(ServiceContext ctx) {
        this.ctx = ctx;
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        assert msg instanceof FullHttpRequest;
        Request req = new Request(((InetSocketAddress)(ctx.channel().remoteAddress())).getAddress(), (FullHttpRequest)msg);
        try {
            doRequest(ctx, req);
        } finally {
            req.destroy();
        }
           
   }

    protected void doRequest(ChannelHandlerContext ctx, Request req) {
        if(req.isSucc()) {
            ServiceInvoker invoker = this.ctx.get(req.xpath());
            if(invoker != null) {
                doInvoke(ctx, req, invoker);
            } else {
                Responses.NOT_FOUND.writeTo(ctx, req);
            }
        } else {
            Responses.BAD_REQUEST.writeTo(ctx, req);
        }
    }

    protected void doInvoke(ChannelHandlerContext ctx, Request req, ServiceInvoker invoker) {
        try {
            sendResponse(ctx, req, invoker.invoke(req));
        } catch (Throwable ex) {
            sendResponse(ctx, req, this.ctx.config().getErrorhandler().handle(req, ex));
        }
    }

   protected void sendResponse(ChannelHandlerContext ctx, Request req, Response resp) {
       resp.writeTo(ctx, req);
   }

   protected void sendNotFoudResponse(ChannelHandlerContext ctx) {
	   Responses.NOT_FOUND.writeTo(ctx, null);
   }

   protected void sendBadRequestResponse(ChannelHandlerContext ctx) {
       Responses.BAD_REQUEST.writeTo(ctx, null);
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
       ctx.close();
   }

}

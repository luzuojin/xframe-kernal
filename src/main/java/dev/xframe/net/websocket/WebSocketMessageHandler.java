package dev.xframe.net.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.net.MessageHandler;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

/**
 * 消息分发器
 * @author luzj
 */
@Sharable
public class WebSocketMessageHandler extends ChannelInboundHandlerAdapter {
	
	protected static Logger logger = LoggerFactory.getLogger(WebSocketMessageHandler.class);
	
	protected WebSocketLifecycleListener listener;
	protected MessageHandler hanlder;
	protected WebSocketServerHandshakerFactory wsFactory;
	
	public WebSocketMessageHandler(WebSocketLifecycleListener listener, MessageHandler hanlder, String wsUrl) {
	    this.listener = listener;
	    this.hanlder = hanlder;
	    this.wsFactory = new WebSocketServerHandshakerFactory(wsUrl, null, false); 
	}

    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
        	handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else if(msg instanceof IMessage) {
        	handleIMessage(Session.get(ctx), (IMessage) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
    	if (req.decoderResult().isSuccess() && "websocket".equalsIgnoreCase(req.headers().get("Upgrade"))) {
    		WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
    		if (handshaker == null) {
    			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel()).addListener(ChannelFutureListener.CLOSE);
    		} else {
    			handshaker.handshake(ctx.channel(), req);
				listener.onSessionRegister(new WebSocketSession(ctx.channel(), listener));
    		}
        } else {
        	ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)).addListener(ChannelFutureListener.CLOSE);
        }
    }

    //处理ping close...
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
        	ctx.writeAndFlush(frame.retain()).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
        	ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //can`t be here
    }

	private void handleIMessage(Session session, IMessage message) {
		listener.onMessageRecieve(session, message);
		try {
			hanlder.run(session, message);
		} catch (Throwable t) {
			listener.onExceptionCaught(session, message, t);
		}
	}

	@Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        listener.onSessionUnRegister(Session.get(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Channel closed", cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if(!ctx.channel().isWritable()) {
            listener.onMessageFlushSlow(Session.get(ctx));
        }
    }

}

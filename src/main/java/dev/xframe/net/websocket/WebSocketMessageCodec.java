package dev.xframe.net.websocket;

import java.util.List;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.MessageCodec;
import dev.xframe.utils.XProperties;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketMessageCodec extends MessageToMessageCodec<WebSocketFrame, IMessage>{
    
    private boolean outFrameUseBinary = XProperties.getAsBool("xframe.websocket.binary", false);
	
	private MessageCodec codec;
	
	public WebSocketMessageCodec(MessageCodec codec) {
		this.codec = codec;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, WebSocketFrame bwsf, List<Object> out) throws Exception {
	    if(bwsf instanceof TextWebSocketFrame || bwsf instanceof BinaryWebSocketFrame) {
	        ByteBuf buf = bwsf.content();
	        IMessage message = codec.decode(ctx, buf);
	        if(message != null) {
	            out.add(message);
	        }
	    }
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, IMessage message, List<Object> out) throws Exception {
		ByteBuf buf = ctx.alloc().ioBuffer();
		codec.encode(ctx, message, buf);
		WebSocketFrame outFrame = outFrameUseBinary ? new BinaryWebSocketFrame(buf) : new TextWebSocketFrame(buf);
        out.add(outFrame);
	}


}

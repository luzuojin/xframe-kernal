package dev.xframe.net.websocket;

import java.util.List;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.MessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebSocketMessageCodec extends MessageToMessageCodec<BinaryWebSocketFrame, IMessage>{
	
	private MessageCodec iCodec;
	
	public WebSocketMessageCodec(MessageCodec iCodec) {
		this.iCodec = iCodec;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame bwsf, List<Object> out) throws Exception {
	    ByteBuf buf = bwsf.content();
	    IMessage message = iCodec.decode(ctx, buf);
	    if(message != null) {
	        out.add(message);
	    }
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, IMessage message, List<Object> out) throws Exception {
		ByteBuf buf = ctx.alloc().ioBuffer();
		iCodec.encode(ctx, message, buf);
        out.add(new BinaryWebSocketFrame(buf));
	}


}

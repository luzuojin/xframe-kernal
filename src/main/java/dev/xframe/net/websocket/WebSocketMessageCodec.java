package dev.xframe.net.websocket;

import java.util.List;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.codec.Message;
import dev.xframe.net.codec.MessageCrypt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebSocketMessageCodec extends MessageToMessageCodec<BinaryWebSocketFrame, Message>{
	
	private MessageCrypt crypt;
	
	public WebSocketMessageCodec(MessageCrypt crypt) {
		this.crypt = crypt;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, List<Object> out) throws Exception {
		crypt.encrypt(ctx, message);
		int bufLen = Message.HDR_SIZE + message.getParamsLen() + message.getBodyLen();
		ByteBuf buf = ctx.alloc().buffer(bufLen);
		message.writeHeader(buf);
        message.writeParams(buf);
        message.writeBody(buf);
        out.add(new BinaryWebSocketFrame(buf));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame bwsf, List<Object> out) throws Exception {
		ByteBuf buf = bwsf.content();
		IMessage message = Message.build();
		message.readHeader(buf);
		message.readParams(buf);
		message.readBody(buf);
		crypt.decrypt(ctx, message);
		out.add(message);
	}

}

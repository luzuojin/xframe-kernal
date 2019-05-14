package dev.xframe.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码
 * @author luzj
 *
 */
public class MessageEncoder extends MessageToByteEncoder<IMessage> {

    public MessageEncoder() {
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, IMessage message, ByteBuf out) throws Exception {
        encode(ctx, message);
        
        message.writeHeader(out);
        message.writeParams(out);
        message.writeBody(out);
    }

    protected void encode(ChannelHandlerContext ctx, IMessage message) {
    }
	
}

package dev.xframe.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码
 * @author luzj
 *
 */
public class NetMessageEncoder extends MessageToByteEncoder<IMessage> {

    private MessageCodec iCodec;
    
    public NetMessageEncoder(MessageCodec iCodec) {
        this.iCodec = iCodec;
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, IMessage message, ByteBuf out) throws Exception {
        iCodec.encode(ctx, message, out);
    }

}

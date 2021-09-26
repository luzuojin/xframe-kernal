package dev.xframe.net.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码
 * @author luzj
 */
public class NetMessageDecoder extends ByteToMessageDecoder {
    
    private MessageCodec codec;
    
    public NetMessageDecoder(MessageCodec codec) {
        this.codec = codec;
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buff, List<Object> out) throws Exception {
        IMessage message = codec.decode(ctx, buff);
        if(message != null) out.add(message);
    }

}

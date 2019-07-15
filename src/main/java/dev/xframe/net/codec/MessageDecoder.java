package dev.xframe.net.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码
 * @author luzj
 */
public class MessageDecoder extends ByteToMessageDecoder {
    
    private MessageCrypt crypt;
    
    public MessageDecoder(MessageCrypt crypt) {
        this.crypt = crypt;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buff, List<Object> out) throws Exception {
        if (buff.readableBytes() < Message.HDR_SIZE) // 不够字节忽略
            return;

        buff.markReaderIndex();
        IMessage message = Message.build();
        message.readHeader(buff);
        int len = message.getBodyLen() + message.getParamsLen();
        if (len > buff.readableBytes()) {
            buff.resetReaderIndex();
            return;
        }
        
        message.readParams(buff);
        message.readBody(buff);
        
        if(crypt != null)
            crypt.decrypt(ctx, message);
        
        out.add(message);
    }

}

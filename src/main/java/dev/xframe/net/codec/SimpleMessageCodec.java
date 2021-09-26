package dev.xframe.net.codec;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

@Bean
@Providable
class SimpleMessageCodec implements MessageCodec {
    
    private MessageCrypt crypt = MessageCrypts.fromSysOps();
    
    protected void setCrypt(MessageCrypt crypt) {
        this.crypt = crypt;
    }

    public final IMessage decode(ChannelHandlerContext ctx, ByteBuf buf) {
        int readableBytes;
        if((readableBytes = Message.readableBytes(buf)) != -1) {
            crypt.decrypt(ctx, buf, readableBytes);
            return Message.readFrom(buf);
        }
        return null;
    }
    
    public final void encode(ChannelHandlerContext ctx, IMessage message, ByteBuf buf) {
        ((WritableMessage) message).writeTo(buf);
        crypt.encrypt(ctx, buf);
    }

}

package dev.xframe.net.codec;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

@Bean
@Providable
public class BuiltinMessageCodec implements MessageCodec {
    
    private MessageCrypt crypt = MessageCrypts.fromSysOps();
    
    protected void setCrypt(MessageCrypt crypt) {
        this.crypt = crypt;
    }

    public final IMessage decode(ChannelHandlerContext ctx, ByteBuf buf) {
        Message message = Message.readFrom(buf);
        if(message != null) {
            crypt.decrypt(ctx, message);
        }
        return message;
    }
    
    public final void encode(ChannelHandlerContext ctx, IMessage message, ByteBuf buf) {
        crypt.encrypt(ctx, message);
        ((BuiltinAbstMessage) message).writeTo(buf);
    }

}

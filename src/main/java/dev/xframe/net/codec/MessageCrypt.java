package dev.xframe.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface MessageCrypt {
    
    public void decrypt(ChannelHandlerContext ctx, ByteBuf byteBuf, int readableBytes);
    
    public void encrypt(ChannelHandlerContext ctx, ByteBuf byteBuf);

}

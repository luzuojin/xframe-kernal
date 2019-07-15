package dev.xframe.net.codec;

import io.netty.channel.ChannelHandlerContext;

public interface MessageCrypt {
    
    public void decrypt(ChannelHandlerContext ctx, IMessage message);
    
    public void encrypt(ChannelHandlerContext ctx, IMessage message);

}

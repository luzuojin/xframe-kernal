package dev.xframe.net.codec;

import dev.xframe.inject.Providable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

@Providable
public interface MessageCodec {

    IMessage decode(ChannelHandlerContext ctx, ByteBuf buf);

        void encode(ChannelHandlerContext ctx, IMessage message, ByteBuf buf);

}
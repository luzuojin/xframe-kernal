package dev.xframe.http.response;

import dev.xframe.http.Request;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface ResponseWriter {

    default void writeTo(ChannelHandlerContext ctx, Request origin) {
        writeTo(ctx.channel(), origin);
    }

    public void writeTo(Channel channel, Request origin);

}

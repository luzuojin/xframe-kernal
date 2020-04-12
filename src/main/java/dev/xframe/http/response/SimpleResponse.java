package dev.xframe.http.response;

import dev.xframe.http.Request;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpResponse;

public abstract class SimpleResponse extends WriterResponse {
	
    public abstract ByteBuf content();
	
	@Override
	public void writeTo(Channel channel, Request origin) {
		ByteBuf content = content();
		int length = content.readableBytes();

		HttpResponse resp = newHttpResp(content);
		
		setBasisHeaders(resp, length);

		//Write the response.
		channel.write(resp);
		channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	}

}
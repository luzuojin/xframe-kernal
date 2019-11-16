package dev.xframe.http.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PlainResponse extends SimpleResponse {
	byte[] bytes;
	public PlainResponse(ContentType type, byte[] bytes) {
		this.set(type);
		this.bytes = bytes;
	}
	public ByteBuf content() {
		return Unpooled.wrappedBuffer(bytes);
	}
}

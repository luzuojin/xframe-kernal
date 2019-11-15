package dev.xframe.http.response;

import io.netty.buffer.ByteBuf;

public class PlainResponse extends SimpleResponse {
	ByteBuf buf;
	public PlainResponse(ContentType type, ByteBuf buf) {
		this.buf = buf;
		this.set(type);
	}
	public ByteBuf content() {
		return buf;
	}
	
	public static class Recyle extends PlainResponse {
		public Recyle(ContentType type, ByteBuf buf) {
			super(type, buf);
		}
		@Override
		public ByteBuf content() {
			buf.retain();
			return buf;
		}
	}
}

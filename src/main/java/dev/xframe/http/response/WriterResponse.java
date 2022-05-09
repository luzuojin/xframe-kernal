package dev.xframe.http.response;

import dev.xframe.http.Response;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Map;

abstract class WriterResponse extends Response implements ResponseWriter {
	
	public WriterResponse() {
		setWriter(this);
	}

	protected HttpResponse newHttpResp(ByteBuf content) {//full
		return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status(), content);
	}
	
	protected void setBasisHeaders(HttpResponse resp, long contentLength) {
		setContentHeaders(resp, contentLength);
		setAccessHeaders(resp);
		setRespHeaders(resp);
	}

	private void setRespHeaders(HttpResponse resp) {
		for(Map.Entry<CharSequence, String> header : headers().entrySet()) {
			resp.headers().set(header.getKey(), header.getValue());
		}
	}
	
	private void setContentHeaders(HttpResponse resp, long contentLength) {
		resp.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType());
		resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
	}
	
	private void setAccessHeaders(HttpResponse resp) {
    	resp.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "86400");
        resp.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        resp.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        resp.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");
        resp.headers().set(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
    }

}

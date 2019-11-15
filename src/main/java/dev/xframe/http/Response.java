package dev.xframe.http;

import java.util.Map;
import java.util.TreeMap;

import dev.xframe.http.response.ContentType;
import dev.xframe.http.response.ResponseWriter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;


/**
 * http response
 * @author luzj
 *
 */
public class Response {
    
	private ResponseWriter writer;
    private ContentType type = ContentType.TEXT;
    private Map<String, String> headers = new TreeMap<>();
	private HttpResponseStatus status = HttpResponseStatus.OK;
	
	protected Response() {
	}
	protected void setWriter(ResponseWriter writer) {
		this.writer = writer;
	}
	
	public Response(ResponseWriter writer) {
		setWriter(writer);
	}
	
    public Map<String, String> headers() {
		return headers;
	}
    
    public String contentType() {
    	return type.val;
    }
    
    public HttpResponseStatus status() { 
    	return status;
    }
    
	public Response set(ContentType type) {
		this.type = type;
		return this;
	}
    
    public Response setHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public Response set(HttpResponseStatus status) {
    	this.status = status;
    	return this;
    }

	public final void writeTo(ChannelHandlerContext ctx, Request origin) {
		writeTo(ctx.channel(), origin);
	}
	public final void writeTo(Channel channel, Request origin) {
		writer.write(channel, origin);
	}

}

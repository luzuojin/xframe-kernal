package dev.xframe.http.response;

import java.io.File;
import java.nio.charset.StandardCharsets;

import dev.xframe.http.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;

public final class Responses {
	
	public static final Response   NOT_FOUND = recycle("Not Found!!!").set(HttpResponseStatus.NOT_FOUND);
    public static final Response BAD_REQUEST = recycle("Bad Request!!!").set(HttpResponseStatus.BAD_REQUEST);
    public static final Response OPTIONS_DEF = recycle("").setHeader("Allow", "*");
    
    private static PlainResponse recycle(String data) {
    	return new PlainResponse.Recyle(ContentType.TEXT, toBytBuf(data));
    }
    
    private static ByteBuf toBytBuf(String data) {
    	return Unpooled.copiedBuffer(data, StandardCharsets.UTF_8);
    }
    
    public static Response of(byte[] bytes) {
    	return new PlainResponse(ContentType.BINARY, Unpooled.copiedBuffer(bytes));
    }
    
    public static Response of(String text) {
    	return new PlainResponse(ContentType.TEXT, toBytBuf(text));
    }
    
    public static Response of(File file) {
    	return new FileResponse.Sys(file);
    }
    
}

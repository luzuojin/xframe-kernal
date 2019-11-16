package dev.xframe.http.response;

import java.io.File;

import dev.xframe.http.Response;
import dev.xframe.utils.XStrings;
import io.netty.handler.codec.http.HttpResponseStatus;

public final class Responses {
	
	public static final Response   NOT_FOUND = of("Not Found!!!").set(HttpResponseStatus.NOT_FOUND);
    public static final Response BAD_REQUEST = of("Bad Request!!!").set(HttpResponseStatus.BAD_REQUEST);
    public static final Response OPTIONS_DEF = of("").setHeader("Allow", "*");
    
    public static Response of(byte[] bytes) {
    	return new PlainResponse(ContentType.BINARY, bytes);
    }
    
    public static Response of(String text) {
    	return new PlainResponse(ContentType.TEXT, XStrings.getBytesUtf8(text));
    }
    
    public static Response of(File file) {
    	return new FileResponse.Sys(file);
    }
    
}

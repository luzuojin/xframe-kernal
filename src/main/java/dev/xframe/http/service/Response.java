package dev.xframe.http.service;

import java.util.Map;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;


/**
 * http response
 * @author luzj
 *
 */
public class Response {
    
    public final IContentType type;
    public final ByteBuf content;
    public final Map<String, String> headers = new TreeMap<>();
    
    public Response(String content) {
        this(ContentType.TEXT, content);
    }
    
    public Response(IContentType type, String content) {
        this.type = type;
        this.content = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
    }
    
    public Response(IContentType type, byte[] content) {
        this.type = type;
        this.content = Unpooled.copiedBuffer(content);
    }
    
    public Response setHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }
    
    public Response retain() {
        content.retain();
        return this;
    }
    
    public static final Response NOT_FOUND = new Response("Not Found!!!");
    public static final Response BAD_REQUEST = new Response("Bad Request!!!");
    public static final Response OPTIONS_DEFAULT = new Response("").setHeader("Allow", "*");

    public static enum ContentType implements IContentType {
        TEXT    ("text/plain; charset=UTF-8"),
        HTML    ("text/html; charset=UTF-8"),
        JSON    ("application/json; charset=UTF-8"),
        JSONP   ("application/javascript; charset=UTF-8"),
        BINARY  ("application/octet-stream"),
        FILE    ("application/octet-stream");
        
        private final String val;
        private ContentType(String val) {
            this.val = val;
        }
        @Override
        public String val() {
            return val;
        }
    }
    
    @FunctionalInterface
    public static interface IContentType {
        String val();
    }
    
    
}

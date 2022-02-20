package dev.xframe.http;

import dev.xframe.http.response.ContentType;
import dev.xframe.http.response.FileResponse;
import dev.xframe.http.response.PlainResponse;
import dev.xframe.http.response.ResponseWriter;
import dev.xframe.utils.XStrings;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;


/**
 * http response
 * @author luzj
 *
 */
public class Response {

    public static final Response   NOT_FOUND = of("Not Found").set(HttpResponseStatus.NOT_FOUND);
    public static final Response BAD_REQUEST = of("Bad Request").set(HttpResponseStatus.BAD_REQUEST);
    public static final Response   FORBIDDEN = of("Forbidden").set(HttpResponseStatus.FORBIDDEN);
    public static final Response       EMPTY = of("");

    public static Response of(byte[] bytes) {
        return of(ContentType.BINARY, bytes);
    }
    public static Response of(String text) {
        return of(ContentType.TEXT, text);
    }
    public static Response of(ContentType type, String text) {
        return of(type, XStrings.getBytesUtf8(text));
    }
    public static Response of(ContentType type, byte[] bytes) {
        return new PlainResponse(type, bytes);
    }
    public static Response of(File file) {
        return new FileResponse.Sys(file);
    }

    private ResponseWriter writer;
    private ContentType type = ContentType.TEXT;
    private Map<CharSequence, String> headers = new TreeMap<>();
    private HttpResponseStatus status = HttpResponseStatus.OK;

    protected Response() {
    }
    protected void setWriter(ResponseWriter writer) {
        this.writer = writer;
    }

    public Response(ResponseWriter writer) {
        setWriter(writer);
    }

    public Map<CharSequence, String> headers() {
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

    public Response setHeader(CharSequence name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Response set(HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    public ResponseWriter getWriter() {
        return writer;
    }

}

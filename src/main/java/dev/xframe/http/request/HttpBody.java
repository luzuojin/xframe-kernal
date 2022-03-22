package dev.xframe.http.request;

import dev.xframe.utils.XStrings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

public class HttpBody {

    static final byte[] Empty = new byte[0];

    transient Object data;
        
    public HttpBody(FullHttpRequest request) {
        if(isMultipart(request)) {
            this.data = new MultiPart(request);
        } else {
            ByteBuf content = request.content();
            if(content.readableBytes() == 0) {
                this.data = Empty;
            } else {
                this.data = ByteBufUtil.getBytes(content);
            }
        }
    }

    private boolean isMultipart(HttpRequest request) {
        String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        return !XStrings.isEmpty(contentType) && contentType.startsWith("multipart/form-data;");
    }

    private Object setData(Object data) {
        return (this.data = data);
    }
    
    public byte[] toBytes() {
    	return (byte[]) data;
    }
    
    public QueryString toQueryString() {
        return (QueryString) (data instanceof byte[] ? setData(new QueryString(XStrings.newStringUtf8((byte[]) data))) : data);
    }
    
    public MultiPart toMultiPart() {
        return (MultiPart) data;
    }

	public void destroy() {
		if(data instanceof MultiPart) {
			((MultiPart) data).destroy();
		}
	}
}

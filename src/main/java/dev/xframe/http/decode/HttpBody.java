package dev.xframe.http.decode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

public class HttpBody {
    
    FullHttpRequest request;
    
    transient Object data;
        
    public HttpBody(FullHttpRequest request) {
        this.request = request;
    }
    
    protected ByteBuf content() { 
        return request.content();
    }
    
    public byte[] toBytes() {
    	return ByteBufUtil.getBytes(content());
    }
    
    public QueryString toQueryString() {
    	return (QueryString) (data == null ? setData(new QueryString(content().toString(CharsetUtil.UTF_8))) : data);
    }
    
    public MultiPart toMultiPart() {
        return (MultiPart) (data == null ? setData(new MultiPart(request)) : data);
    }
    
	private Object setData(Object data) {
		return (this.data = data);
	}

	public void destroy() {
		if(data instanceof MultiPart) {
			((MultiPart) data).destroy();
		}
		request.release();
	}
    
}

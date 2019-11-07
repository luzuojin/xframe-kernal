package dev.xframe.http.decode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HttpBody implements LastHttpContent {
    
    private static final int MAX_BODY_BYTES = 1 * 1024 * 1024;
    
    HttpRequest request;
    
        ByteBuf byteBuf;
        
         Object body;
    
    public HttpBody(HttpRequest request) {
        this.request = request;
    }
    
    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public void offer(HttpContent content) {
        if(byteBuf == null) {
            byteBuf = Unpooled.buffer();
        }
        
        byteBuf.writeBytes(content.content());
        
        if(byteBuf.readableBytes() > MAX_BODY_BYTES) {
            throw new IllegalArgumentException("Http server max body size is 1M");
        }
    }
    
    public byte[] toBytes() {
        if(body == null) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            body = bytes;
        }
        return (byte[]) body;
    }
    
    public QueryString toQueryString() {
        if(body == null) {
            body = new QueryString(byteBuf.toString(CharsetUtil.UTF_8));
        }
        return (QueryString) body;
    }
    
    public MultiPart toMultiPart() {
        if(body == null) {
            body = new MultiPart(this);
        }
        return (MultiPart) body;
    }
    
	@Override
	public DecoderResult decoderResult() {
		return request.decoderResult();
	}

	@Override
	public ByteBuf content() {
		return byteBuf;
	}

	@Override
	public int refCnt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean release() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean release(int decrement) {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpHeaders trailingHeaders() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setDecoderResult(DecoderResult result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LastHttpContent copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public LastHttpContent duplicate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public LastHttpContent retainedDuplicate() {
		throw new UnsupportedOperationException();
	}
	@Override
	public LastHttpContent replace(ByteBuf content) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public LastHttpContent retain(int increment) {
		throw new UnsupportedOperationException();
	}
	@Override
	public LastHttpContent retain() {
		throw new UnsupportedOperationException();
	}

	@Override
	public LastHttpContent touch() {
		throw new UnsupportedOperationException();
	}
	@Override
	public LastHttpContent touch(Object hint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DecoderResult getDecoderResult() {
		return request.decoderResult();
	}


}

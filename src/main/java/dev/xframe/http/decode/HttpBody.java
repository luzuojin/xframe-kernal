package dev.xframe.http.decode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HttpBody {
    
    private static final int MAX_BODY_BYTES = 1 * 1024 * 1024;
    
    HttpRequest request;
    
        ByteBuf byteBuf;
        
         Object data;
        
    public HttpBody(HttpRequest request) {
        this.request = request;
    }
    
    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public void offer(HttpContent content) {
    	ByteBuf buffer = content.content();
    	if (buffer != null) {
            if (buffer.readableBytes() > MAX_BODY_BYTES) {
            	throw new IllegalArgumentException("Out of max http body size(1M)");
            }
            if (byteBuf == null) {
                byteBuf = buffer;
            } else if (byteBuf instanceof CompositeByteBuf) {
                CompositeByteBuf cbb = (CompositeByteBuf) byteBuf;
                cbb.addComponent(true, buffer);
            } else {
                CompositeByteBuf cbb = Unpooled.compositeBuffer(Integer.MAX_VALUE);
                cbb.addComponents(true, byteBuf, buffer);
                byteBuf = cbb;
            }
        }
        if(byteBuf.readableBytes() > MAX_BODY_BYTES) {
            throw new IllegalArgumentException("Out of max http body size(1M)");
        }
    }
    
    public byte[] toBytes() {
    	return ByteBufUtil.getBytes(byteBuf);
    }
    
    public QueryString toQueryString() {
    	return (QueryString) (data == null ? setData(new QueryString(byteBuf.toString(CharsetUtil.UTF_8))) : data);
    }
    
    public MultiPart toMultiPart() {
        return (MultiPart) (data == null ? setData(new MultiPart(request).offer(new HttpBodyContent())) : data);
    }
    
	private Object setData(Object data) {
		return (this.data = data);
	}

	public void destroy() {
		if(data instanceof MultiPart) {
			((MultiPart) data).destroy();
		}
		byteBuf.release();
	}
    
    //for multi part decoder
    class HttpBodyContent implements LastHttpContent {
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

}

package dev.xframe.http.request;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;

public class MultiPart extends HttpPostMultipartRequestDecoder {

	public MultiPart(HttpRequest request) {
		super(request);
	}

}

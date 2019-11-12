package dev.xframe.http.decode;

import java.util.List;
import java.util.Set;

import io.netty.handler.codec.http.QueryStringDecoder;

public class QueryString extends QueryStringDecoder implements HttpParams {

	public QueryString(String uri) {
		this(uri, false);
	}
	
	public QueryString(String uri, boolean hasPath) {
		super(uri, hasPath);
	}
	
	@Override
	public Set<String> getParamNames() {
		return parameters().keySet();
	}
	
	@Override
	public List<String> getParamValues(String name) {
		return parameters().get(name);
	}

}


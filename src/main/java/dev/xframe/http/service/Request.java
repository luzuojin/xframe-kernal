package dev.xframe.http.service;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import dev.xframe.http.decode.HttpBody;
import dev.xframe.http.decode.HttpURI;
import dev.xframe.http.decode.HttpParams;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

/**
 * 
 * http request
 * 
 * @author luzj
 *
 */
public class Request implements HttpParams {
    
    private boolean succ;
    
    private InetAddress address;
    private HttpHeaders headers;
    private HttpMethod method;
    
    private HttpBody body;
    private HttpURI uri;
    
    public Request(InetAddress address, FullHttpRequest request) {
        this(address, request.uri(), request.headers(), request.method());
        this.succ = request.decoderResult().isSuccess();
        this.body = new HttpBody(request);
    }
    
    public Request(InetAddress address, String uri, HttpHeaders headers, HttpMethod method) {
        this.address = address;
        this.uri = new HttpURI(uri);
        this.headers = headers;
        this.method = method;
        this.succ = true;
    }

    public byte[] content() {
        return this.body == null ? null : body.toBytes();
    }
    
    public HttpBody body() {
        return this.body;
    }

    public String remoteHost() {
        return this.address.getHostAddress();
    }
    
    public InetAddress address() {
        return this.address;
    }
    
    public String getHeader(String name) {
        return this.headers.get(name);
    }
    
    public HttpHeaders headers() {
        return this.headers;
    }
    
    public HttpMethod method() {
        return this.method;
    }
    
    public String uri() {
        return uri.uri();
    }
    
    public String queryString() {
        return uri.rawQuery();
    }
    
    /**
     * Returns the decoded path string of the URI.
     */
    public String path() {
        return uri.path();
    }
    
    /**
     * @return trim '/'
     */
    public String xpath() {
        return uri.xpath();
    }
    
    public Set<String> getParamNames() {
        return uri.parameters().keySet();
    }
    public List<String> getParamValues(String name) {
        return uri.parameters().get(name);
    }
    
    public boolean isSucc() {
        return succ;
    }
    
    //release all resources.
	public void destroy() {
		if(body != null)
			body.destroy();
	}
    
}

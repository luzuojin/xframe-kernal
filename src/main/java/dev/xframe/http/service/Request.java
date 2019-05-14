package dev.xframe.http.service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.xframe.http.decode.HttpBody;
import dev.xframe.http.decode.HttpURI;
import dev.xframe.http.decode.IParameters;
import dev.xframe.http.decode.QueryString;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

/**
 * 
 * http request
 * 
 * @author luzj
 *
 */
public class Request implements IParameters {
    
    private InetAddress address;
    private HttpRequest request;
    private HttpHeaders headers;
    private HttpMethod method;
    
    private HttpBody body;
    private HttpURI uri;
    
    private List<Throwable> causes;
   
    public Request(InetAddress address, HttpRequest request) {
        this(address, request.getUri(), request.headers(), request.getMethod());
        this.request = request;
        this.appendDecoderResult(this.request.getDecoderResult());
    }
    public Request(InetAddress address, String uri, HttpHeaders headers, HttpMethod method) {
        this.address = address;
        this.uri = new HttpURI(uri);
        this.headers = headers;
        this.method = method;
    }

    public List<Throwable> causes() {
        return causes;
    }
    
    public byte[] content() {
        return this.body == null ? null : body.asBytes();
    }
    
    public HttpBody body() {
        return this.body;
    }

    public void appendDecoderResult(DecoderResult dr) {
        if(dr.isSuccess()) {
            return;
        }
        if(causes == null) {
            causes = new ArrayList<Throwable>();
        }
        causes.add(dr.cause());
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
        return uri.queryString();
    }
    
    
    /**
     * Returns the decoded path string of the URI.
     */
    public String path() {
        return uri.path();
    }
    
    public String purePath() {
        return uri.purePath();
    }
    
    public Set<String> getParamNames() {
        return uri.parameters().keySet();
    }
    public List<String> getParamValues(String name) {
        return uri.parameters().get(name);
    }
    
    public void appendContent(HttpContent content) {
        if(content.getDecoderResult().isSuccess()) {
            if(this.body == null) {
                this.body = new HttpBody(request);
            }
            this.body.offer(content);
        } else {
            this.appendDecoderResult(content.getDecoderResult());
        }
    }

    public boolean isSuccess() {
        return this.causes == null;
    }
    
    public static class Params extends QueryString implements IParameters {
        public Params(String uri) {
            super(uri, false);
        }
        @Override
        public Set<String> getParamNames() {
            return parameters().keySet();
        }
        @Override
        public List<String> getParamValues(String name) {
            return parameters().get(name);
        }
        
        public static Params decode(String queryString) {
            return new Params(queryString);
        }
    }

}

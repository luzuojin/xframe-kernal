package dev.xframe.http.service.rest;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.Response;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.config.RespEncoder;
import dev.xframe.http.service.path.PathMatcher;
import io.netty.handler.codec.http.HttpMethod;

public class RestServiceInvoker implements Service {
	
	final RespEncoder respEncoder;
	final RestService service;
	
	public RestServiceInvoker(RestService service, RespEncoder respEncoder) {
		this.service = service;
		this.respEncoder = respEncoder;
	}
	
	@Override
	public Response exec(Request req, PathMatcher matcher) throws Throwable {
		HttpMethod method = req.method();
		if(HttpMethod.DELETE.equals(method)) {
		    return respEncoder.encode(service.delete(req, matcher));
		}
		if(HttpMethod.PUT.equals(method)) {
		    return respEncoder.encode(service.put(req, matcher));
		}
		if(HttpMethod.POST.equals(method)) {
		    return respEncoder.encode(service.post(req, matcher));
		}
		if(HttpMethod.OPTIONS.equals(method)) {
		    return respEncoder.encode(service.options(req, matcher));
		}
		return respEncoder.encode(service.get(req, matcher));//default get
	}

	@Override
	public Response exec(Request req) throws Throwable {
		return exec(req, PathMatcher.TRUE);//can`t be here
	}

    @Override
    public String toString() {
        return service.toString();
    }

}

package dev.xframe.http.service.rest;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.Response;
import dev.xframe.http.service.Service;
import dev.xframe.http.service.uri.PathMatcher;
import io.netty.handler.codec.http.HttpMethod;

public class ServiceAdapter implements Service {
	
	final RespEncoder respEncoder;
	final RestService service;
	
	public ServiceAdapter(RestService service, RespEncoder respEncoder) {
		this.service = service;
		this.respEncoder = respEncoder;
	}
	
	@Override
	public Response service(Request req, PathMatcher matcher) {
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
	public Response service(Request req) {
		return service(req, PathMatcher.TRUE);//can`t be here
	}

    @Override
    public String toString() {
        return service.toString();
    }

}

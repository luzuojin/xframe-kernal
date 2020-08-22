package dev.xframe.http.service;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.service.config.HttpInterceptor;
import dev.xframe.http.service.config.ServiceConfig;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import io.netty.channel.ChannelHandlerContext;

@Bean
public class ServiceHandler {
	
	@Inject
	private ServiceConfig config;
	@Inject
	private ServiceContext serCtx;
	
    public final void exec(ChannelHandlerContext ctx, Request req) {
    	Response resp = Response.NOT_FOUND;
    	HttpInterceptor interceptor = config.getInterceptor();
    	Service s = serCtx.get(req.xpath());
    	if(s != null) {
    		try {
    			resp = interceptor.intercept(req);
    			if(resp == null) {
    				resp = s.exec(req);
    			}
    			interceptor.afterHandle(req, resp);
    		} catch (Throwable ex) {
    			resp = config.getErrorhandler().handle(req, ex);
    		}
    	}
        resp.getWriter().writeTo(ctx, req);
    }

}

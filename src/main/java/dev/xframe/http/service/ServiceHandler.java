package dev.xframe.http.service;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.config.HttpConfig;
import dev.xframe.http.config.HttpInterceptor;
import dev.xframe.http.config.HttpListener;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Providable;
import io.netty.channel.ChannelHandlerContext;

@Bean
@Providable
public class ServiceHandler {

    @Inject
    private HttpConfig config;
    @Inject
    private ServiceContext serCtx;

    public void exec(ChannelHandlerContext ctx, Request req) {
        HttpInterceptor interceptor = config.getInterceptor();
        HttpListener listener = config.getListener();
        
        listener.onAccessStarting(req);
        
        Response resp;
        Service s = serCtx.get(req.xpath());
        if(s != null) {
            try {
                resp = interceptor.intercept(req);
                if(resp == null) {
                    resp = s.exec(req);
                    listener.onAccessComplete(req, resp);
                }
            } catch (Throwable ex) {
                resp = config.getErrorhandler().handle(req, ex);
                listener.onExceptionCaught(req, ex);
            }
        } else {
            resp = Response.NOT_FOUND;
            listener.onServiceNotFound(req);
        }
        
        resp.getWriter().writeTo(ctx, req);
    }

}

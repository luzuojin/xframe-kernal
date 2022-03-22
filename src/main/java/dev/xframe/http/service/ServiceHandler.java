package dev.xframe.http.service;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.http.config.ErrorHandler;
import dev.xframe.http.config.HttpConfig;
import dev.xframe.http.config.HttpInterceptor;
import dev.xframe.http.config.HttpListener;
import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Providable;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executor;

@Bean
@Providable
public class ServiceHandler implements Loadable {

    @Inject
    protected HttpConfig config;
    @Inject
    protected ServiceContext serCtx;

    protected ErrorHandler errorHandler;
    protected HttpListener httpListener;
    protected HttpInterceptor httpInterceptor;
    protected Executor serviceExecutor;

    @Override
    public void load() {
        errorHandler = config.getErrorhandler();
        httpListener = config.getListener();
        httpInterceptor = config.getInterceptor();
        serviceExecutor = config.getServiceExecutor();
    }

    public final void exec(ChannelHandlerContext ctx, Request req) {
        serviceExecutor.execute(()->{
            try {
                Service service = serCtx.get(req.xpath());
                if (service == null) {
                    httpListener.onServiceNotFound(req);
                    Response.NOT_FOUND.getWriter().writeTo(ctx, req);
                    return;
                }
                httpListener.onAccessStarting(req);

                Response resp;
                if ((resp = httpInterceptor.intercept(req)) != null) {
                    onResponded(ctx, req, resp);
                } else {
                    resp = exec0(service, req);
                    onResponded(ctx, req, resp);
                }
            } catch (Throwable ex) {
                onExceptionCaught(ctx, req, ex);
            } finally {
                req.destroy();
            }
        });
    }

    protected Response exec0(Service service, Request req) throws Exception {
        return service.exec(req);
    }

    protected void onResponded(ChannelHandlerContext ctx, Request req, Response resp) {
        resp.getWriter().writeTo(ctx, req);
        httpListener.onAccessComplete(req, resp);
    }

    protected void onExceptionCaught(ChannelHandlerContext ctx, Request req, Throwable ex) {
        errorHandler.handle(req, ex).getWriter().writeTo(ctx, req);
        httpListener.onExceptionCaught(req, ex);
    }
}

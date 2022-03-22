package dev.xframe.http.config;

import dev.xframe.http.Response;
import dev.xframe.http.service.rest.ArgParsers;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Providable;
import dev.xframe.utils.XStrings;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Configurator
@Providable
public class HttpConfigSetter implements HttpConfig, Loadable {
    
    @Inject
    private HttpInterceptor interceptor;
    @Inject
    private HttpListener listener;

    private Executor serviceExecutor;
    private ErrorHandler errorhandler;
    private BodyDecoder bodyDecoder;
    private RespEncoder respEncoder;

    @Override
    public final HttpInterceptor getInterceptor() {
        return interceptor;
    }
    @Override
    public HttpListener getListener() {
        return listener;
    }
    @Override
    public Executor getServiceExecutor() {
        return serviceExecutor;
    }
    @Override
    public final ErrorHandler getErrorhandler() {
        return errorhandler;
    }
    @Override
    public final BodyDecoder getBodyDecoder() {
        return bodyDecoder;
    }
    @Override
    public final RespEncoder getRespEncoder() {
        return respEncoder;
    }

    @Override
    public final void load() {
        setInterceptor(v->interceptor=v);
        setListener(v->listener=v);
        setServiceExecutor(v->serviceExecutor=v);

        setErrorHandler(v->errorhandler=v);
        setBodyDecoder(v->bodyDecoder=v);
        setRespEncoder(v->respEncoder=v);

        setArgParser(ArgParsers::offer);
    }

    public void setServiceExecutor(Consumer<Executor> setter) {
        setter.accept(Runnable::run);//run directly
    }
    public void setInterceptor(Consumer<HttpInterceptor> setter) {
        //...
    }
    public void setListener(Consumer<HttpListener> setter) {
        //...
    }

    public void setErrorHandler(Consumer<ErrorHandler> setter) {
        setter.accept((r, e) -> Response.of(XStrings.getStackTrace(e)));
    }
    public void setBodyDecoder(Consumer<BodyDecoder> setter) {
        setter.accept((t, b) -> b);
    }
    public void setRespEncoder(Consumer<RespEncoder> setter) {
        setter.accept(o -> (Response) o);
    }

    public void setArgParser(ArgParserSetter setter) {
        //...
    }
}

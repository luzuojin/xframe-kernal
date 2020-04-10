package dev.xframe.http.service.config;

import java.util.function.Consumer;

import dev.xframe.http.Response;
import dev.xframe.http.response.Responses;
import dev.xframe.inject.Configurator;
import dev.xframe.inject.Loadable;
import dev.xframe.inject.Providable;
import dev.xframe.utils.XStrings;

@Configurator
@Providable
public class ServiceConfigSetter implements ServiceConfig, Loadable {
    
    private HttpInterceptor interceptor;
    private ErrorHandler errorhandler;
    
    private BodyDecoder bodyDecoder;
    private RespEncoder respEncoder;
    
    @Override
    public final HttpInterceptor getInterceptor() {
        return interceptor;
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
        setErrorHandler(v->errorhandler=v);
        
        setBodyDecoder(v->bodyDecoder=v);
        setRespEncoder(v->respEncoder=v);
    }

    public void setInterceptor(Consumer<HttpInterceptor> setter) {
        setter.accept(r -> null);
    }

    public void setErrorHandler(Consumer<ErrorHandler> setter) {
        setter.accept((r, e) -> Responses.of(XStrings.getStackTrace(e)));
    }

    public void setBodyDecoder(Consumer<BodyDecoder> setter) {
        setter.accept((t, b) -> b);
    }

    public void setRespEncoder(Consumer<RespEncoder> setter) {
        setter.accept(o -> (Response) o);
    }

}

package dev.xframe.http.service.config;

import java.util.function.Consumer;

import dev.xframe.http.service.Response;
import dev.xframe.injection.Configurator;
import dev.xframe.injection.Loadable;
import dev.xframe.injection.Providable;
import dev.xframe.utils.XStrings;

@Configurator
@Providable
public class ServiceConfigSetter implements ServiceConfig, Loadable {
    
    private RequestInteceptor inteceptor;
    private ErrorHandler errorhandler;
    private FileHandler fileHandler;
    
    private BodyDecoder bodyDecoder;
    private RespEncoder respEncoder;
    
    @Override
    public final RequestInteceptor getInteceptor() {
        return inteceptor;
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
    public final FileHandler getFileHandler() {
        return fileHandler;
    }

    @Override
    public final void load() {
        setIncepetor(v->inteceptor=v);
        setErrorHandler(v->errorhandler=v);
        setFileHandler(v->fileHandler=v);
        
        setBodyDecoder(v->bodyDecoder=v);
        setRespEncoder(v->respEncoder=v);
    }

    public void setIncepetor(Consumer<RequestInteceptor> setter) {
        setter.accept(r -> null);
    }

    public void setErrorHandler(Consumer<ErrorHandler> setter) {
        setter.accept((r, e) -> new Response(XStrings.getStackTrace(e)));
    }

    public void setBodyDecoder(Consumer<BodyDecoder> setter) {
        setter.accept((t, b) -> b);
    }

    public void setRespEncoder(Consumer<RespEncoder> setter) {
        setter.accept(o -> (Response) o);
    }

    public void setFileHandler(Consumer<FileHandler> setter) {
        setter.accept(p -> null);
    }
    
}
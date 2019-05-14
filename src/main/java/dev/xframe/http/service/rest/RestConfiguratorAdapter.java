package dev.xframe.http.service.rest;

import dev.xframe.http.service.ErrorHandler;
import dev.xframe.http.service.FileHandler;
import dev.xframe.http.service.RequestInteceptor;
import dev.xframe.http.service.Response;
import dev.xframe.injection.Configurator;
import dev.xframe.injection.Loadable;
import dev.xframe.injection.Providable;
import dev.xframe.tools.XStrings;

@Configurator
@Providable
public class RestConfiguratorAdapter implements RestConfig, Loadable {
    
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

    public void setIncepetor(IncepetorSetter setter) {
        setter.set(r -> null);
    }

    public void setErrorHandler(ErrorHandlerSetter setter) {
        setter.set((r, e) -> new Response(XStrings.getStackTrace(e)));
    }

    public void setBodyDecoder(BodyDecoderSetter setter) {
        setter.set((t, b) -> b);
    }

    public void setRespEncoder(RespEncoderSetter setter) {
        setter.set(o -> (Response) o);
    }

    public void setFileHandler(FileHandlerSetter setter) {
        setter.set(p -> null);
    }
    
    public interface IncepetorSetter {
        void set(RequestInteceptor inteceptor);
    }
    public interface BodyDecoderSetter {
        void set(BodyDecoder bodyDecoder);
    }
    public interface RespEncoderSetter {
        void set(RespEncoder respEncoder);
    }
    public interface FileHandlerSetter {
        void set(FileHandler fileHandler);
    }
    public interface ErrorHandlerSetter {
        void set(ErrorHandler errorHandler);
    }
}

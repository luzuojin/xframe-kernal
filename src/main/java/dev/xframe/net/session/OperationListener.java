package dev.xframe.net.session;

import java.util.function.Consumer;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public interface OperationListener extends ChannelFutureListener {
    
    @Override
    default void operationComplete(ChannelFuture future) throws Exception {
        if(!future.isSuccess()) {
            onFailure(future.cause());
        } else {
            onComplete();
        }
    }

    void onFailure(Throwable cause);

    void onComplete();
    
    static OperationListener of(Consumer<Throwable> onFailure) {
        return of(null, onFailure);
    }
    static OperationListener of(Runnable onComplete) {
        return of(onComplete, null);
    }
    static OperationListener of(Runnable onComplete, Consumer<Throwable> onFailure) {
        return new OperationListener() {
            @Override
            public void onFailure(Throwable cause) {
                if(onFailure != null) {
                    onFailure.accept(cause);
                }
            }
            @Override
            public void onComplete() {
                if(onComplete != null) {
                    onComplete.run();
                }
            }
        };
    }
    
}

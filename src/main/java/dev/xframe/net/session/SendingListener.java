package dev.xframe.net.session;

import java.util.function.Consumer;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public interface SendingListener extends ChannelFutureListener {
    
    @Override
    default void operationComplete(ChannelFuture future) throws Exception {
        if(!future.isSuccess()) {
            failure(future.cause());
        } else {
            complete();
        }
    }

    void failure(Throwable cause);

    void complete();
    
    static SendingListener of(Consumer<Throwable> failure) {
        return new DefSendingListener(null, failure);
    }
    
    static SendingListener of(Runnable complete) {
        return new DefSendingListener(complete, null);
    }
    
    static class DefSendingListener implements SendingListener {
        final Runnable complete;
        final Consumer<Throwable> failure;
        public DefSendingListener(Runnable complete, Consumer<Throwable> failure) {
            this.complete = complete;
            this.failure = failure;
        }
        @Override
        public void failure(Throwable cause) {
            if(failure != null) {
                failure.accept(cause);;
            }
        }
        @Override
        public void complete() {
            if(complete != null) {
                complete.run();
            }
        }
    }
    
}

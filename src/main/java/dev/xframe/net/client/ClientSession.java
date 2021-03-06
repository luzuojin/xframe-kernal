package dev.xframe.net.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.ChannelSession;
import dev.xframe.net.session.Session;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class ClientSession extends ChannelSession {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private AtomicBoolean isConnecting;
    
    public ClientSession(LifecycleListener listener, Bootstrap bootstrap, String host, int port) {
        super(listener);
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.isConnecting = new AtomicBoolean(false);
    }

    @Override
    public boolean connect() {
        if (this.isActive()) return true;
        if (isConnecting.get()) return false;
        if (bootstrap.config().group().isShutdown()) return false;
        
        try {
            if(isConnecting.compareAndSet(false, true)) {
                if(!this.isActive()) {
                    ChannelFuture future = bootstrap.connect(host, port);
                    future.await(1, TimeUnit.SECONDS);
                    Channel channel = future.channel();
                    if(channel != null && channel.isActive()) {
                        initial(channel);
                    } else {
                        logFailure(future.cause());
                    }
                }
            }
        } catch (Throwable e) {
            logFailure(e);
            //ignore
        } finally {
            isConnecting.set(false);
        }
        return this.isActive();
    }
    
    public Session syncConnect() throws InterruptedException {
        if (!isActive()) {
            initial(bootstrap.connect(host, port).sync().channel());
        }
        return this;
    }
    
    private void initial(Channel channel) {
        bindChannel(channel);
        listener.onSessionRegister(this);
    }

    private void logFailure(Throwable e) {
        logger.warn("session connect[{}:{}] failed, cause {}", host, port, e.getMessage());
    }

}

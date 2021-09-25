package dev.xframe.net.client;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.ChannelSession;
import dev.xframe.net.session.OperationListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class ClientSession extends ChannelSession {
    
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private final AtomicBoolean connecting;
    
    public ClientSession(LifecycleListener listener, Bootstrap bootstrap, String host, int port) {
        super(listener);
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.connecting = new AtomicBoolean(false);
    }

    @Override
    public void connect(OperationListener opListener) {
        if(isConnectAvailable() && connecting.compareAndSet(false, true)) {
            if(this.isActive()) {
                connecting.set(false);
                return;
            }
            bootstrap.connect(host, port)
                    .addListener((ChannelFuture f) -> {
                        connecting.set(false);
                        if(f.isSuccess()) {
                            onConnected(f.channel());
                            opListener.onComplete();
                        } else {
                            opListener.onFailure(f.cause());
                        }
                    });
        }
    }

    private boolean isConnectAvailable() {
        return !(isActive() || connecting.get() || bootstrap.config().group().isShutdown());
    }
    
    /**
     * sync connect
     * @throws InterruptedException
     */
    public ClientSession connect() throws InterruptedException {
        if(isConnectAvailable()) {
            onConnected(bootstrap.connect(host, port).sync().channel());
        }
        return this;
    }

    private void onConnected(Channel channel) {
        Channel ex = this.channel;
        bindChannel(channel);
        if(ex != null && ex != channel && ex.isOpen()) {
            if(ex.hasAttr(SESSION)) {//clear session attach
                ex.attr(SESSION).set(null);
            }
            ex.close();//ensure close ex (may repeated close)
        }
    }

}

package dev.xframe.net.client;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import dev.xframe.net.LifecycleListener;
import io.netty.bootstrap.Bootstrap;

@Bean
@Providable
public class ClientSessionFactory {
    public ClientSession newSession(LifecycleListener listener, Bootstrap bootstrap, String host, int port) {
        return new ClientSession(listener, bootstrap, host, port);
    }
}

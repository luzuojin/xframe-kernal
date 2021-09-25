package dev.xframe.net.server;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Providable;
import dev.xframe.net.LifecycleListener;
import io.netty.channel.Channel;

@Bean
@Providable
public class ServerSessionFactory {
    public ServerSession newSession(Channel channel, LifecycleListener listener) {
        return new ServerSession(channel, listener);
    }
}

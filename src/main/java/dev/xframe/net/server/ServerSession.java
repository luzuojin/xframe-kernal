package dev.xframe.net.server;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.ChannelSession;
import io.netty.channel.Channel;

/**
 * 管理channel handler context
 * @author luzj
 *
 */
public class ServerSession extends ChannelSession {

    public ServerSession(Channel channel, LifecycleListener listener) {
        super(listener);
        bindChannel(channel);
    }
    
    @Override
    public boolean reconnect() {
        throw new IllegalArgumentException("Server session don`t support reconnect!");
    }

}

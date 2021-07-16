package dev.xframe.net.server;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.ChannelSession;
import dev.xframe.net.session.OperationListener;
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
    public void connect(OperationListener opListener) {
        throw new UnsupportedOperationException("ServerSession don`t support connect!");
    }

}

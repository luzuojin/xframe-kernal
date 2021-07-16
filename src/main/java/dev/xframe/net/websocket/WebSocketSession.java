package dev.xframe.net.websocket;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.ChannelSession;
import dev.xframe.net.session.OperationListener;
import io.netty.channel.Channel;

public class WebSocketSession extends ChannelSession {
	
	protected WebSocketSession(Channel channel, LifecycleListener listener) {
		super(listener);
		bindChannel(channel);
	}

	@Override
	public void connect(OperationListener opListener) {
		throw new UnsupportedOperationException("WebSocketSession don`t support connect!");
	}

}

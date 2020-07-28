package dev.xframe.net.websocket;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.session.ChannelSession;
import io.netty.channel.Channel;

public class WebSocketSession extends ChannelSession {
	
	protected WebSocketSession(Channel channel, LifecycleListener listener) {
		super(listener);
		bindChannel(channel);
	}

	@Override
	public boolean connect() {
		throw new UnsupportedOperationException("WebSocketSession don`t support connect!");
	}

}

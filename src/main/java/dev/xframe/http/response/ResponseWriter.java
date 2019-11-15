package dev.xframe.http.response;

import dev.xframe.http.Request;
import io.netty.channel.Channel;

public interface ResponseWriter {
	
	void write(Channel channel, Request origin);
	
}

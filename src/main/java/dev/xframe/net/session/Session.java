package dev.xframe.net.session;

import dev.xframe.net.codec.IMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public abstract class Session {
    
    protected static final AttributeKey<Session> SESSION = AttributeKey.valueOf("SESSION");
    public static Session get(ChannelHandlerContext channel) {
        return get(channel.channel());
    }
    public static Session get(Channel channel) {
        return channel.attr(SESSION).get();
    }
    
    public abstract long id();
    
    public abstract Channel channel();
    
    public abstract Session bind(long id);
    
    public abstract void sendMessage(IMessage message);
    
    public abstract void sendMessage(IMessage message, SendingListener slistener);

    public abstract void sendMessageAndClose(IMessage message);

    public abstract boolean isActive();

    public abstract boolean reconnect();

    public abstract void close();

}
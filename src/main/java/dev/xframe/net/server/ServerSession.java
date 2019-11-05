package dev.xframe.net.server;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.SendingListener;
import dev.xframe.net.session.Session;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 管理channel handler context
 * @author luzj
 *
 */
public class ServerSession extends Session {

    protected long id;
    protected Channel channel;
    protected LifecycleListener listener;
    
    protected ServerSession(LifecycleListener listener) {
        this.listener = listener;
    }
    
    public ServerSession(Channel channel, LifecycleListener listener) {
        this(listener);
        this.bindChannel(channel);
    }

    protected void bindChannel(Channel channel) {
        this.channel = channel;
        this.channel.attr(SESSION).set(this);
    }
    
    @Override
    public Channel channel() {
        return this.channel;
    }
    
    @Override
    public long id() {
        return this.id;
    }

    @Override
    public Session bind(long id) {
        this.id = id;
        return this;
    }

    @Override
    public void sendMessage(IMessage message, SendingListener slistener) {
        listener.onMessageSending(this, message);
        
        channel.writeAndFlush(message).addListener(slistener);
    }

    @Override
    public void sendMessage(IMessage message) {
        listener.onMessageSending(this, message);

        channel.writeAndFlush(message);
    }
    
    @Override
    public void sendMessageAndClose(IMessage message) {
        listener.onMessageSending(this, message);

        channel.write(message);
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
    
    @Override
    public boolean isActive() {
        return this.channel != null && this.channel.isActive();
    }
    
    @Override
    public boolean reconnect() {
        throw new IllegalArgumentException("Server session don`t support reconnect!");
    }

    @Override
    public void close() {
        if(channel != null && channel.isOpen()) {
            channel.close();
        }
    }

}

package dev.xframe.net.session;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.codec.IMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public abstract class ChannelSession extends Session {

    protected long id;
    protected Channel channel;
    protected LifecycleListener listener;
    
    protected ChannelSession(LifecycleListener listener) {
        this.listener = listener;
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
    public void sendMessage(IMessage message, OperationListener opListener) {
        listener.onMessageSending(this, message);
        
        channel.writeAndFlush(message).addListener(opListener);
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
    public void close() {
        if(channel != null && channel.isOpen()) {
            channel.close();
        }
    }

}

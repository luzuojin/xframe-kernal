package dev.xframe.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * 消息分发器
 * @author luzj
 *
 */
@Sharable
public class NetMessageHandler extends ChannelInboundHandlerAdapter {
	
	protected static Logger logger = LoggerFactory.getLogger(NetMessageHandler.class);
	
	protected LifecycleListener listener;
	protected MessageHandler hanlder;
	
	public NetMessageHandler(LifecycleListener listener, MessageHandler hanlder) {
	    this.listener = listener;
	    this.hanlder = hanlder;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    IMessage req = (IMessage) msg;
	    Session session = Session.get(ctx);
	    listener.onMessageRecieve(session, req);
		try {
		    hanlder.run(session, req);
		} catch (Throwable ex) {
            listener.onExceptionCaught(session, req, ex);
        }
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Channel closed", cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if(!ctx.channel().isWritable()) {
            listener.onMessageFlushSlow(Session.get(ctx));
        }
    }

}

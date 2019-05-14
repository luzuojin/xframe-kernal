package dev.xframe.net.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageInterceptor;
import dev.xframe.net.cmd.Command;
import dev.xframe.net.cmd.CommandContext;
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
	
	protected CommandContext cmds;
	protected LifecycleListener listener;
	protected MessageInterceptor interceptor;
	
	public NetMessageHandler(LifecycleListener listener, CommandContext cmds, MessageInterceptor interceptor) {
	    this.listener = listener;
	    this.cmds = cmds;
	    this.interceptor = interceptor;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		IMessage req = (IMessage) msg;
		Session session = Session.get(ctx);
		if(interceptor == null || (!interceptor.intercept(session, req))) {//被拦截的情况下不执行
		    exec(session, req);
		}
	}

    private void exec(Session session, IMessage req) {
        
        listener.onMessageRecieve(session, req);
        
        int code = req.getCode();
        Command cmd = cmds.get(code);
        if(cmd == null) {
            logger.warn("Can't find the command: " + code);
            return;
        }
        
        try {
	        cmd.execute(session, req);
	    } catch (Throwable ex) {
	        listener.onCmdException(session, cmd, req, ex);
	    }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        listener.onSessionUnRegister(Session.get(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Channel closed", cause);
        ctx.close();
    }

}

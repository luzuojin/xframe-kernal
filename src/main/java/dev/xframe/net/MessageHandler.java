package dev.xframe.net;

import dev.xframe.net.cmd.CommandHandler;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public class MessageHandler {
	
	private MessageInterceptor interceptor;
	private CommandHandler cmdHandler;
	
	public MessageHandler(MessageInterceptor interceptor, CommandHandler cmdHandler) {
		this.interceptor = interceptor;
		this.cmdHandler = cmdHandler;
	}

    public void run(Session session, IMessage message) throws Exception {
    	if(!interceptor.intercept(session, message)) {
    		cmdHandler.exec(session, message);
    	}
    }
    
}

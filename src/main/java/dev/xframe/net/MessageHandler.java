package dev.xframe.net;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public interface MessageHandler {

    public boolean handle(Session session, IMessage message) throws Exception;
    
}

package dev.xframe.net;

import java.util.LinkedList;

import dev.xframe.net.cmd.CommandHandler;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public class MessageHandlerPipeline implements MessageHandler {
    
    private LinkedList<MessageHandler> list = new LinkedList<>();
    
    private MessageHandler tail = new CommandHandler();//最后由本地cmd处理
    
    public MessageHandlerPipeline addFrist(MessageHandler handler) {
        if(handler != null) {
            list.addFirst(handler);
        }
        return this;
    }
    
    public MessageHandlerPipeline addLast(MessageHandler handler) {
        if(handler != null) {
            list.addLast(handler);
        }
        return this;
    }
    
    @Override
    public boolean handle(Session session, IMessage message) throws Exception {
        for (MessageHandler mh : list) {
            if(mh.handle(session, message)) {
                return true;
            }
        }
        return tail.handle(session, message);
    }

}

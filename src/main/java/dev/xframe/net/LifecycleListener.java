package dev.xframe.net;

import dev.xframe.injection.Combine;
import dev.xframe.net.cmd.Command;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

@Combine
public interface LifecycleListener {
    
    public void onSessionRegister(Session session);
    
    public void onMessageRecieve(Session session, IMessage message);
    
    public void onMessageSending(Session session, IMessage message);
    
    public void onSessionUnRegister(Session session);

    public void onCmdException(Session session, Command cmd, IMessage req, Throwable ex);

}

package dev.xframe.net.cmd;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XLogger;

@Prototype
public class CommandHandler implements MessageHandler {
    
    @Inject
    private CommandContext cmdCtx;

    @Override
    public boolean handle(Session session, IMessage req) throws Exception {
        Command cmd = cmdCtx.get(req.getCode());
        if(cmd != null) {
            cmd.execute(session, req);
        } else {
            XLogger.warn("cmd[{}] not found", req.getCode());
        }
        return true;
    }

}

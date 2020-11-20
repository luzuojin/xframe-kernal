package dev.xframe.net.cmd;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Providable;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XLogger;

@Bean
@Providable
public class CommandHandler {
    
    @Inject
    private CommandContext cmdCtx;

    public void exec(Session session, IMessage req) throws Exception {
        Command cmd = cmdCtx.get(req.getCode());
        if(cmd != null) {
            cmd.execute(session, req);
        } else {
            XLogger.warn("cmd[{}] not found", req.getCode());
        }
    }

}

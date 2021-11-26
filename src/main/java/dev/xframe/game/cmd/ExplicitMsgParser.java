package dev.xframe.game.cmd;

import dev.xframe.net.codec.IMessage;

/**
 * IMessage to explicit msg 
 * @author luzj
 */
@FunctionalInterface
public interface ExplicitMsgParser {
    
    @SuppressWarnings("unchecked")
    default <T> T parse(IMessage req) {
        return (T) parse0(req);
    }
    
    public Object parse0(IMessage req);
    
}

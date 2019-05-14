package dev.xframe.game.player;

import dev.xframe.injection.Combine;
import dev.xframe.net.codec.IMessage;

@Combine
public interface CmdErrorHandler {
    
    public void onError(long playerId, Class<?> cmd, IMessage req, Throwable tx);

}

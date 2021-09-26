package dev.xframe.net.codec;

import io.netty.buffer.ByteBuf;

public interface WritableMessage {
    
    public void writeTo(ByteBuf buff);

}

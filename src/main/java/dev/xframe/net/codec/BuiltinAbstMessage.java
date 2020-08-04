package dev.xframe.net.codec;

import io.netty.buffer.ByteBuf;

interface BuiltinAbstMessage extends IMessage {

    //for codec
    void writeTo(ByteBuf buff);
    
    //for crypt
    void setFlag(short flag);

    void setCode(int code);
    
    int getBodyLen();

}

package dev.xframe.net.codec;

import io.netty.buffer.ByteBuf;


public interface IMessage {
    
    /**
     * body length
     * 包含在header里边
     */
    int getBodyLen();
    
    short getParamsLen();
    
    /**
     * 玩家的唯一标识符, 从PlayerContext里边取得玩家
     * @return
     */
    long getId();
    
    int getCode();
    
    byte[] getBody();
    
    void addParam(String key, String value);
    
    String getParam(String key);
    
    void readHeader(ByteBuf buff);
    
    void readParams(ByteBuf buff);

    void readBody(ByteBuf buff);

    void writeHeader(ByteBuf buff);
    
    void writeParams(ByteBuf buff);

    void writeBody(ByteBuf buff);

    short getFlag();
    
    void setFlag(short flag);

    void setCode(int code);
    
    int getVersion();

}
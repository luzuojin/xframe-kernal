package dev.xframe.net.codec;

public interface IMessage {
    
    short getFlag();
    /**
     * 玩家的唯一标识符, 从PlayerContext里边取得玩家
     * @return
     */
    long getId();
    
    int getCode();
    
    int getVersion();
    
    <T> T getBody();
    
    String getParam(String key);

}
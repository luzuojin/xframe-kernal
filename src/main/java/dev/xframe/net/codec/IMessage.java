package dev.xframe.net.codec;

public interface IMessage {
    
    short getFlag();
    
    long getId();
    
    int getCode();
    
    int getVersion();
    
    <T> T getBody();
    
    String getParam(String key);

}
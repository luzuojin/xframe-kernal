package dev.xframe.event;

import java.util.function.Consumer;

public interface Registrator {
    
    public void regist(Subscriber subscriber);
    
    public default void regist(int group, Consumer<?> sub) {
        regist(Subscriber.of(group, sub));
    }
    
    public default void regist(Consumer<?> sub) {
        regist(0, sub);//use default group [0]
    }
    
    public void unregist(int group);
    
    public void unregist(Subscriber subscriber);
    
}

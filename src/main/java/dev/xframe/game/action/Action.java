package dev.xframe.game.action;

/**
 * handle msg action
 * msg can be data Pojo or Runnable instance
 */
public interface Action<T extends Actor, M> {
    
    void exec(T actor, M msg) throws Exception;

}

package dev.xframe.game.action;

@FunctionalInterface
public interface Runnable<T extends Actor> {
    void run(T actor) throws Exception;
}

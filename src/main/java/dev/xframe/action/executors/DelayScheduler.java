package dev.xframe.action.executors;

import dev.xframe.action.DelayAction;

public interface DelayScheduler {
    void startup();
    void shutdown();
    void checkin(DelayAction action);
}
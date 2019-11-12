package dev.xframe.utils;

import io.netty.util.concurrent.FastThreadLocalThread;

public class XThread extends FastThreadLocalThread {

    public XThread(ThreadGroup group, Runnable r, String name) {
        super(group, r, name);
    }

}

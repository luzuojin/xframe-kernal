package dev.xframe.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {
    
    static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    
    AppRunner appRunner = new AppRunner();
    Networker networker = new Networker();
    
    public Bootstrap include(String includes) {
        appRunner.include(includes);
        return this;
    }
    
    public Bootstrap exclude(String excludes) {
        appRunner.exclude(excludes);
        return this;
    }
    
    /**
     * using for record pid file
     * @System.Property#logs.dir
     * @return
     */
    public Bootstrap withName(String name) {
        appRunner.withName(name);
        return this;
    }
    
    public Bootstrap withTcp(int port) {
        networker.withTcp(port);
        return this;
    }
    public Bootstrap withTcp(int port, int nThreads) {
        networker.withHttp(port, nThreads);
        return this;
    }
    public Bootstrap withWebSocket(String host, int port) {
        networker.withWebSocket(host, port);
        return this;
    }
    public Bootstrap withWebSocket(String host, int port, int nThreads) {
        networker.withWebSocket(host, port, nThreads);
        return this;
    }
    
    public Bootstrap withHttp(int port) {
         networker.withHttp(port);
         return this;
    }
    public Bootstrap withHttp(int port, int nThreads) {
        networker.withHttp(port, nThreads);
        return this;
    }
    
    public Bootstrap startup() {
        try {
            appRunner.startup();
            
            networker.startup();
        } catch (Throwable ex) {
            logger.error("Startup failed!", ex);
            System.exit(-1);
        }
        return this;
    }
    
}

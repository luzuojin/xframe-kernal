package dev.xframe.boot;

import dev.xframe.http.HttpServer;
import dev.xframe.net.NetServer;
import dev.xframe.net.WebSocketServer;
import dev.xframe.utils.XProperties;
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

    /**
     * startup from xproperties
     * @return
     */
    public static Bootstrap fromProperties() {
        //basic keys
        String kName    = "xframe.boot.name";
        String kInclude = "xframe.boot.include";
        String kExclude = "xframe.boot.exclude";
        //http keys
        String kHttpPort    = "xframe.boot.http.port";
        String kHttpThreads = "xframe.boot.http.threads";
        //tcp keys
        String kTcpPort    = "xframe.boot.tcp.port";
        String kTcpThreads = "xframe.boot.tcp.threads";
        //ws keys
        String kWsPort    = "xframe.boot.websocket.port";
        String kWsHost    = "xframe.boot.websocket.host";
        String kWsThreads = "xframe.boot.websocket.threads";

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.withName(XProperties.get(kName));
        bootstrap.include(XProperties.get(kInclude));
        bootstrap.exclude(XProperties.get(kExclude));
        int httpPort = XProperties.getAsInt(kHttpPort);
        if (httpPort > 0) {
            bootstrap.withHttp(httpPort, XProperties.getAsInt(kHttpThreads, HttpServer.defaultThreads()));
        }
        int tcpPort = XProperties.getAsInt(kTcpPort);
        if (tcpPort > 0) {
            bootstrap.withHttp(tcpPort, XProperties.getAsInt(kTcpThreads, NetServer.defaultThreads()));
        }
        int wsPort = XProperties.getAsInt(kWsPort);
        if (wsPort > 0) {
            bootstrap.withWebSocket(XProperties.get(kWsHost), wsPort, XProperties.getAsInt(kWsThreads, WebSocketServer.defaultThreads()));
        }
        return bootstrap;
    }
    public static void main(String[] args) {
        fromProperties().startup();
    }
}

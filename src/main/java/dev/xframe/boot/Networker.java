package dev.xframe.boot;

import dev.xframe.http.HttpServer;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Ordered;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetServer;
import dev.xframe.net.WebSocketServer;
import dev.xframe.net.cmd.CommandHandler;
import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.gateway.Gateway;
import dev.xframe.net.server.ServerLifecycleListener;
import dev.xframe.net.server.ServerMessageInterceptor;
import dev.xframe.net.websocket.WebSocketLifecycleListener;
import dev.xframe.net.websocket.WebSocketMessageInterceptor;

@Ordered(Integer.MAX_VALUE)
public class Networker implements ShutdownAgent {
    
    @Inject
    CommandHandler cmdHandler;
    @Inject
    ServerLifecycleListener sLifecycleListener;
    @Inject
    ServerMessageInterceptor sMessageInterceptor;
    @Inject
    WebSocketLifecycleListener wsLifecycleListener;
    @Inject
    WebSocketMessageInterceptor wsMessageInterceptor;
    @Inject
    ShutdownHook shutdownHook;
    @Inject
    MessageCodec iCodec;
    
    Gateway gateway;
    
    int tcpPort;
    int tcpThreads;
    NetServer tcp;
    
    int httpPort;
    int httpThreads;
    HttpServer http;
    
    String wsHost;
    int wsPort;
    int wsThreads;
    WebSocketServer ws;
    
    public Networker withTcp(int port) {
        return withTcp(port, NetServer.defaultThreads());
    }
    public Networker withTcp(int port, int nThreads) {
        this.tcpPort = port;
        this.tcpThreads = nThreads;
        return this;
    }
    public Networker useGateway(Gateway gateway) {
        this.gateway = gateway;
        return this;
    }
    public Networker withWebSocket(String host, int port) {
    	return withWebSocket(host, port, WebSocketServer.defaultThreads());
    }
    public Networker withWebSocket(String host, int port, int nThreads) {
    	this.wsHost = host;
    	this.wsPort = port;
    	this.wsThreads = nThreads;
		return this;
	}
    
	public Networker withHttp(int port) {
        return withHttp(port, HttpServer.defaultThreads());
    }
    public Networker withHttp(int port, int nThreads) {
        this.httpPort = port;
        this.httpThreads = nThreads;
        return this;
    }
    
    public void startup() {
        try {
            BeanHelper.inject(this);
            shutdownHook.append(this);
            
            if(tcpPort > 0) {
                tcp = new NetServer().setCodec(iCodec).setThreads(tcpThreads).setPort(tcpPort).setListener(sLifecycleListener).setHandler(newMessageHandler()).startup();
            }
            if(wsPort > 0) {
            	ws = new WebSocketServer().setCodec(iCodec).setThreads(wsThreads).setHost(wsHost).setPort(wsPort).setListener(wsLifecycleListener).setHandler(newMessageHandler()).startup();
            }
            if(httpPort > 0) {
                http = new HttpServer().setThreads(httpThreads).setPort(httpPort).startup();
            }
        } catch (Throwable ex) {
            Bootstrap.logger.error("Startup failed!", ex);
            System.exit(-1);
        }
    }
    
    private MessageHandler newMessageHandler() {
        return new MessageHandler(sMessageInterceptor, (gateway != null) ? gateway : cmdHandler);
    }

    public void shutdown() {
        if(http != null) http.shutdown();
        if(tcp != null) tcp.shutdown();
        if(ws != null) ws.shutdown();
    }

}

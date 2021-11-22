package dev.xframe.boot;

import java.util.LinkedList;
import java.util.List;

import dev.xframe.http.HttpServer;
import dev.xframe.http.service.ServiceHandler;
import dev.xframe.inject.Inject;
import dev.xframe.inject.Ordered;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetServer;
import dev.xframe.net.WebSocketServer;
import dev.xframe.net.cmd.CommandHandler;
import dev.xframe.net.codec.MessageCodec;
import dev.xframe.net.server.ServerLifecycleListener;
import dev.xframe.net.server.ServerMessageInterceptor;
import dev.xframe.net.server.ServerSessionFactory;
import dev.xframe.net.websocket.WebSocketLifecycleListener;
import dev.xframe.net.websocket.WebSocketMessageInterceptor;

@Ordered(Integer.MAX_VALUE)
public class Networker implements ShutdownAgent {
    
    @Inject
    private ShutdownHook shutdownHook;
    
    private List<INetBootstrap<?>> boostraps = new LinkedList<>();
    
    private Networker append(INetBootstrap<?> iBootstrap) {
        boostraps.add(iBootstrap);
        return this;
    }
    
    public Networker withTcp(int port) {
        return withTcp(port, NetServer.defaultThreads());
    }
    public Networker withTcp(int port, int nThreads) {
        return append(new TcpBootstrap().setPort(port).setThreads(nThreads));
    }
    
    public Networker withWebSocket(String host, int port) {
    	return withWebSocket(host, port, WebSocketServer.defaultThreads());
    }
    public Networker withWebSocket(String host, int port, int nThreads) {
        return append(new WsBootstrap().setHost(host).setPort(port).setThreads(nThreads));
	}
    
	public Networker withHttp(int port) {
        return withHttp(port, HttpServer.defaultThreads());
    }
    public Networker withHttp(int port, int nThreads) {
        return append(new HttpBootstrap().setPort(port).setThreads(nThreads));
    }
    
    public void startup() {
        try {
            BeanHelper.inject(this);
            shutdownHook.append(this);
            
            for (INetBootstrap<?> e : boostraps) {
                BeanHelper.inject(e).startup();
            }
        } catch (Throwable ex) {
            Bootstrap.logger.error("Startup failed!", ex);
            System.exit(-1);
        }
    }

    @Override
    public void shutdown() {
        for (INetBootstrap<?> e : boostraps) {
            e.shutdown();
        }
    }
    
    
    static abstract class INetBootstrap<T> {
        int port;
        int threads;
        T   server;
        INetBootstrap<T> setPort(int port) {
            this.port = port;
            return this;
        }
        INetBootstrap<T> setThreads(int threads) {
            this.threads = threads;
            return this;
        }
        final void startup() {
            this.server = startup0();
        }
        abstract T    startup0();
        abstract void shutdown();
    }
    static class TcpBootstrap extends INetBootstrap<NetServer> {
        @Inject
        MessageCodec messageCodec;
        @Inject
        CommandHandler cmdHandler;
        @Inject
        ServerLifecycleListener lifecycleListener;
        @Inject
        ServerMessageInterceptor messageInterceptor;
        @Inject
        ServerSessionFactory sessionFactory;
        @Override
        NetServer startup0() {
            return new NetServer()
                    .setPort(port)
                    .setThreads(threads)
                    .setCodec(messageCodec)
                    .setListener(lifecycleListener)
                    .setFactory(sessionFactory)
                    .setHandler(new MessageHandler(messageInterceptor, cmdHandler))
                    .startup();
        }
        @Override
        void shutdown() {
            server.shutdown();
        }
    }
    static class WsBootstrap extends INetBootstrap<WebSocketServer> {
        @Inject
        MessageCodec messageCodec;
        @Inject
        CommandHandler cmdHandler;
        @Inject
        WebSocketLifecycleListener lifecycleListener;
        @Inject
        WebSocketMessageInterceptor messageInterceptor;
        String host;
        WsBootstrap setHost(String host) {
            this.host = host;
            return this;
        }
        @Override
        WebSocketServer startup0() {
            return new WebSocketServer()
                    .setHost(host)
                    .setPort(port)
                    .setThreads(threads)
                    .setCodec(messageCodec)
                    .setListener(lifecycleListener)
                    .setHandler(new MessageHandler(messageInterceptor, cmdHandler))
                    .startup();
        }
        @Override
        void shutdown() {
            server.shutdown();
        }
    }
    static class HttpBootstrap extends INetBootstrap<HttpServer> {
        @Inject
        ServiceHandler serviceHandler;
        @Override
        HttpServer startup0() {
            return new HttpServer()
                    .setPort(port)
                    .setThreads(threads)
                    .setHandler(serviceHandler)
                    .startup();
        }
        @Override
        void shutdown() {
            server.shutdown();
        }
    }
}

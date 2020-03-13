package dev.xframe.net.gateway;

import java.util.LinkedList;
import java.util.List;

import dev.xframe.inject.Inject;
import dev.xframe.inject.Prototype;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.MessageHandlerPipeline;
import dev.xframe.net.NetClient;
import dev.xframe.net.client.ClientLifecycleListener;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;
import dev.xframe.utils.XLogger;

@Prototype
public class Gateway implements MessageHandler {
    
    @Inject
    private ClientLifecycleListener lifecycleListener;
    
    private NetClient netClient;
    private Connector connector;
    private List<Route> routes = new LinkedList<>();
    
    public Gateway() {
        netClient = new NetClient();
        connector = new Connector(netClient);
        netClient.setHandler(new MessageHandlerPipeline().addLast(this));
        SyntheticBuilder.append(lifecycleListener, connector);//监听连接被断开
        netClient.setListener(lifecycleListener);
    }
    
    public Gateway setThreads(int threads) {
        netClient.setThreads(threads);
        return this;
    }
    public Gateway setHeartbeat(int heartbeatCode) {
        netClient.setHeartbeat(heartbeatCode);
        return this;
    }
    
    public Gateway add(Route route) {
        route.upstream().setConnector(connector);
        routes.add(route);
        return this;
    }
    
    public void post(IMessage message) {
        for (Route route : routes) {
            if(route.test(message)) {
                route.post(message);
                return;
            }
        }
        XLogger.warn("None route post message[{}]", message.getCode());
    }

    @Override
    public boolean handle(Session session, IMessage message) {
        post(message);
        return true;
    }
    
}

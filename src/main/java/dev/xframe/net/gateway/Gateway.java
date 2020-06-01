package dev.xframe.net.gateway;

import java.util.LinkedList;
import java.util.List;

import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.BeanHelper;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.net.MessageHandler;
import dev.xframe.net.NetClient;
import dev.xframe.net.client.ClientLifecycleListener;
import dev.xframe.net.client.ClientMessageInterceptor;
import dev.xframe.net.cmd.CommandHandler;
import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.Session;

public class Gateway extends CommandHandler {
    
    @Inject
    private ClientLifecycleListener lifecycleListener;
    @Inject
    private ClientMessageInterceptor interceptor;
    
    private NetClient netClient;
    private Connector connector;
    private List<Route> routes = new LinkedList<>();
    
    public Gateway() {
    	BeanHelper.inject(this);
        netClient = new NetClient();
        connector = new Connector(netClient);
        netClient.setHandler(new MessageHandler(interceptor, this));
        SyntheticBuilder.append(lifecycleListener, connector);//监听连接被断开
        netClient.setListener(lifecycleListener);
    }
    
    public Gateway setThreads(int threads) {
        netClient.setThreads(threads);
        return this;
    }
    
    public Gateway add(Route route) {
        route.upstream().setConnector(connector);
        routes.add(route);
        return this;
    }
    
    public boolean post(IMessage message) {
        for (Route route : routes) {
            if(route.test(message)) {
                route.post(message);
                return true;
            }
        }
        return false;
    }

    @Override
	public void exec(Session session, IMessage req) throws Exception {
    	boolean posted = post(req);
    	if(!posted) {
    		super.exec(session, req);
    	}
	}
    
}

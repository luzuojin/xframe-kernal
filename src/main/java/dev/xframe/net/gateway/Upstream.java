package dev.xframe.net.gateway;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.GroupSession;

public class Upstream {
    
    private String host;
    private int tcpPort;
    private int connCnt;
    
    private Connector connector;
    private GroupSession gs;
    
    public Upstream(String host, int port) {
        this(host, port, 4);
    }
    public Upstream(String host, int port, int connCnt) {
        this.host = host;
        this.tcpPort = port;
        this.connCnt = connCnt;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }
    
    GroupSession getGS() {
        return gs == null ? create() : gs;
    }
    
    synchronized GroupSession create() {
        if(gs == null) {
            GroupSession _gs = new GroupSession();
            for (int i = 0; i < connCnt; i++) {
                _gs.add(connector.connect(host, tcpPort));
            }
            gs = _gs;
        }
        return gs;
    }

    public void post(IMessage message) {
        getGS().sendMessage(message);
    }

}

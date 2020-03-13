package dev.xframe.net.gateway;

import dev.xframe.net.codec.IMessage;
import dev.xframe.net.session.GroupSession;
import dev.xframe.net.session.Session;

public class Upstream {
    
    private String host;
    private int tcpPort;
    private int connCnt;
    
    private boolean initiated;
    
    private Connector connector;
    private GroupSession gs = new GroupSession();
    
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
    
    void initial() {
        if(initiated)
            return;
        initial0();
    }
    
    synchronized void initial0() {
        if(!initiated) {
            for (int i = 0; i < connCnt; i++) {
                Session s = connector.connect(host, tcpPort);
                s.connect();
                gs.add(s);
            }
            initiated = true;
        }
    }

    public void post(IMessage message) {
        initial();
        
        gs.sendMessage(message);
    }

}

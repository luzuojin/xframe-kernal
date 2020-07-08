package dev.xframe.net.gateway;

import dev.xframe.net.codec.IMessage;

/**
 * 转发路径, 通过协议号(IMessage.code)区分
 * @author luzj
 */
public class Route {
    
    private String name;
    
    private Locator locator;

    private Upstream upstream;
    
    public Route name(String name) {
        this.name = name;
        return this;
    }
    
    public Route upstream(Upstream upstream) {
        this.upstream = upstream;
        return this;
    }
    
    public Route locator(Locator locator) {
        this.locator = locator;
        return this;
    }
    
    public String name() {
        return name;
    }
    
    public Upstream upstream() {
        return upstream;
    }
    
    public Locator locator() {
        return locator;
    }
    
    boolean test(IMessage message) {
        return locator.test(message);
    }
    
    void post(IMessage message) {
        upstream.post(message);
    }

}

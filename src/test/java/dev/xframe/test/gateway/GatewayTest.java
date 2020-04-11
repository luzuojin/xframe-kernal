package dev.xframe.test.gateway;

import org.junit.Test;
import org.junit.runner.RunWith;

import dev.xframe.inject.junit.ContextScan;
import dev.xframe.inject.junit.Junit4ClassRunner;
import dev.xframe.net.codec.Message;
import dev.xframe.net.gateway.Gateway;
import dev.xframe.net.gateway.Locator;
import dev.xframe.net.gateway.Route;
import dev.xframe.net.gateway.Upstream;

@RunWith(Junit4ClassRunner.class)
@ContextScan(includes = "dev.xframe.*")
public class GatewayTest {
    
    @Test
    public void test() {
        Gateway gateway = new Gateway()
                            //远程连接管理线程池大小&心跳协议号
                                .setThreads(4)
                            //转发规则
                                .add(new Route()//一个上游服务器
                                        .name("xcat_001")
                                        .upstream(new Upstream("127.0.0.1", 7500, 2))
                                        .locator(new Locator()//匹配到一个协议号段 && 无Filter或者匹配到一个Filter
                                                .range(11000, 12000)
                                                .range(21000, 22000)
                                                .filter(m->m.getId()==10086)
                                                .filter(m->m.getId()==10010)
                                                )
                                        )
                                .add(new Route()
                                        .name("xcat_002")
                                        .upstream(new Upstream("127.0.0.1", 7600))
                                        .locator(new Locator()
                                                .range(2000, 3000)
                                                .filter(m->m.getId()>Short.MAX_VALUE)
                                                )
                                        )
                                ;
        
        //bootstrap.useGateway(gateway);
        gateway.post(Message.build(11001).copy(10010));//post message
    }

}

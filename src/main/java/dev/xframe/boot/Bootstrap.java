package dev.xframe.boot;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.http.HttpServer;
import dev.xframe.http.service.ServiceContext;
import dev.xframe.injection.ApplicationContext;
import dev.xframe.injection.Inject;
import dev.xframe.injection.Injection;
import dev.xframe.net.LifecycleListener;
import dev.xframe.net.MessageInterceptor;
import dev.xframe.net.NetServer;
import dev.xframe.net.cmd.CommandContext;
import dev.xframe.tools.XProcess;

public class Bootstrap {
    
    public static Bootstrap RUNNING_INSTANCE;
    
    static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    
    String name;
    
    String includes = "*";
    String excludes = "";
    
    int tcpPort;
    int tcpThreads;
    NetServer tcp;
    
    int httpPort;
    int httpThreads;
    HttpServer http;
    
    @Inject
    LifecycleListener lifecycleListener;
    @Inject
    CommandContext commandCtx;
    @Inject
    ServiceContext serviceCtx;
    @Inject
    MessageInterceptor interceptor;
    
    public Bootstrap() {
    	if(RUNNING_INSTANCE != null) {
    		logger.error("Program is running...");
            System.exit(-1);
    	} else {
    		RUNNING_INSTANCE = this;
    	}
    }
    
    public Bootstrap include(String includes) {
        this.includes = includes;
        return this;
    }
    
    public Bootstrap exclude(String excludes) {
        this.excludes = excludes;
        return this;
    }
    
    /**
     * using for record pid file
     * @System.Property#logs.dir
     * @return
     */
    public Bootstrap withName(String name) {
        this.name = name;
        return this;
    }
    
    public static int defTcpThreads() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }
    public Bootstrap withTcp(int port) {
        return withTcp(port, defTcpThreads());
    }
    public Bootstrap withTcp(int port, int nThreads) {
        this.tcpPort = port;
        this.tcpThreads = nThreads;
        return this;
    }
    
    public static int defHttpThreads() {
        return Runtime.getRuntime().availableProcessors();
    }
    public Bootstrap withHttp(int port) {
        return withHttp(port, defHttpThreads());
    }
    public Bootstrap withHttp(int port, int nThreads) {
        this.httpPort = port;
        this.httpThreads = nThreads;
        return this;
    }
    
    public Bootstrap startup() {
        try {
            String pfile = Paths.get(System.getProperty("logs.dir", System.getProperty("user.home")), name + ".pid").toString();
            if(XProcess.isProcessRunning(pfile)) {
                logger.error("Program is running...");
                System.exit(-1);
            }
            XProcess.writeProcessIdFile(pfile);
            
            ApplicationContext.initialize(includes, excludes);
            
            Injection.inject(this);
            
            if(tcpPort > 0) {
                tcp = new NetServer();
                tcp.start(tcpPort, tcpThreads, commandCtx, interceptor, lifecycleListener);
            }

            if(httpPort > 0) {
                http = new HttpServer();
                http.start(httpPort, httpThreads, serviceCtx);
            }
        } catch (Throwable ex) {
            logger.error("Startup failed!", ex);
            System.exit(-1);
        }
        return this;
    }
    
    public Bootstrap shutdown() {
        if(http != null) http.stop();
        if(tcp != null) tcp.stop();
        return this;
    }
    
}

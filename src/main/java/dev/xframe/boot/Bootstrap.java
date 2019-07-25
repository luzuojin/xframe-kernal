package dev.xframe.boot;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.xframe.http.HttpServer;
import dev.xframe.injection.ApplicationContext;
import dev.xframe.net.NetServer;
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
    
    public Bootstrap withTcp(int port) {
        return withTcp(port, NetServer.defaultThreads());
    }
    public Bootstrap withTcp(int port, int nThreads) {
        this.tcpPort = port;
        this.tcpThreads = nThreads;
        return this;
    }
    
    public Bootstrap withHttp(int port) {
        return withHttp(port, HttpServer.defaultThreads());
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
            
            if(tcpPort > 0) {
                tcp = new NetServer().working(tcpThreads).listening(tcpPort).startup();
            }

            if(httpPort > 0) {
                http = new HttpServer().working(httpThreads).listening(httpPort).startup();
            }
        } catch (Throwable ex) {
            logger.error("Startup failed!", ex);
            System.exit(-1);
        }
        return this;
    }
    
    public Bootstrap shutdown() {
        if(http != null) http.shutdown();
        if(tcp != null) tcp.shutdown();
        return this;
    }
    
}

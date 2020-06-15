package dev.xframe.boot;

import java.nio.file.Paths;

import dev.xframe.utils.XProcess;

public class Processor {
    
    private String name;
    
    public void withName(String name) {
        this.name = name;
    }
    
    public void startup() {
        String pfile = Paths.get(System.getProperty("logs.dir", System.getProperty("user.home")), name + ".pid").toString();
        if(XProcess.isProcessRunning(pfile)) {
            Bootstrap.logger.error("Program is running...");
            System.exit(-1);
        }
        XProcess.writeProcessIdFile(pfile);
    }
    
}

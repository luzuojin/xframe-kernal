package dev.xframe.boot;

import java.nio.file.Paths;

import dev.xframe.utils.XProcess;
import dev.xframe.utils.XProperties;

public class Processor {
    
    private String name;
    
    public void withName(String name) {
        this.name = name;
    }
    
    public void startup() {
        String pfile = Paths.get(XProperties.get("logs.dir", XProperties.get("user.home")), name + ".pid").toString();
        if(XProcess.isProcessRunning(pfile)) {
            Bootstrap.logger.error("Program is running...");
            System.exit(-1);
        }
        XProcess.writeProcessIdFile(pfile);
    }
    
}

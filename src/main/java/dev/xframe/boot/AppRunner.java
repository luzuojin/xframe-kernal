package dev.xframe.boot;

import java.nio.file.Paths;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.utils.XProcess;
import dev.xframe.utils.XProperties;

public class AppRunner {
    
    String name = "xframe";
    
    String includes = "*";
    String excludes = "";
    
    public AppRunner withName(String name) {
        this.name = name;
        return this;
    }
    
    public AppRunner include(String includes) {
        this.includes = includes;
        return this;
    }
    public AppRunner exclude(String excludes) {
        this.excludes = excludes;
        return this;
    }
    
    public void startup() {
        startupProcessor ();
        startupAppContext();
    }
    
    private void startupProcessor() {
        String pfile = Paths.get(XProperties.get("logs.dir", XProperties.get("user.home")), name + ".pid").toString();
        if(XProcess.isProcessRunning(pfile)) {
            Bootstrap.logger.error("Program is running...");
            System.exit(-1);
        }
        XProcess.writeProcessIdFile(pfile);
    }
    
    private void startupAppContext() {
        ApplicationContext.initialize(includes, excludes);
    }
    
}

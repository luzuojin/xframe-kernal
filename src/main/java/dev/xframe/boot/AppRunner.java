package dev.xframe.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import dev.xframe.inject.ApplicationContext;
import dev.xframe.utils.XProperties;
import dev.xframe.utils.XStrings;

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
        if(isProcessRunning(pfile)) {
            Bootstrap.logger.error("Program is running...");
            System.exit(-1);
        }
        writeProcessIdFile(pfile);
    }
    
    public static boolean isProcessRunning(String pidfile) {
        try {
            String pid = new String(Files.readAllBytes(Paths.get(pidfile)));
            List<String> processes = jprocesses();
            for (String process : processes) {
                if(process.startsWith(pid)) return true;
            }
        } catch (IOException e) {
            //ignore
        }
        return false;
    }
    
    public static void writeProcessIdFile(String pidfile) {
        try {
            Files.write(Paths.get(pidfile), currentProcessId().getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            //ignore
        }
    }

    public static String currentProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.substring(0, name.indexOf("@"));
    }
    
    private void startupAppContext() {
        ApplicationContext.initialize(includes, excludes);
    }
    
    private static List<String> jprocesses() throws IOException {
        return execCmd(jpsCmd());
    }

    private static String jpsCmd() {
        String jhome = System.getenv("JAVA_HOME");
        if(!XStrings.isEmpty(jhome)) {
            return String.format("%s/bin/jps", jhome);
        }
        return "jps";
    }

    private static List<String> execCmd(String cmd) throws IOException {
        BufferedReader reader = null;
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } finally {
            if (reader != null) reader.close();
        }
    }
}

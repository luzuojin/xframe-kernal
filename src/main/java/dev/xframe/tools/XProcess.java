package dev.xframe.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class XProcess {
    
    public static List<String> jprocesses() throws IOException {
        return execCmd(jpsCmd());
    }

    public static String jpsCmd() {
        String jhome = System.getenv("JAVA_HOME");
        if(!XStrings.isEmpty(jhome)) {
            return String.format("%s/bin/jps", jhome);
        }
        return "jps";
    }

    public static List<String> execCmd(String cmd) throws IOException {
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
    
}

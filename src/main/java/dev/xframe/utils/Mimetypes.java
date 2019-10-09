package dev.xframe.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Mimetypes {
    
    static final String TYPE_DEFAULT = "application/octet-stream";
    
    static final Map<String, String> types = new HashMap<>();
    
    static {
        try {
            Properties properties = new Properties();
            properties.load(getResourceAsStream("mimetypes.default"));
            for(Object key : properties.keySet()) {
                String keystr = key.toString();
                types.put(keystr, properties.getProperty(keystr));
            }
        } catch (IOException e) {
            //ignore
        }
    }
    
    public static String get(String file) {
        if(file != null) {
            int index = file.lastIndexOf(".");
            if(index != -1) {
                String type = types.get(file.substring(index));
                if(type != null) {
                    return type;
                }
            }
        }
        return TYPE_DEFAULT;
    }

    
    static InputStream getResourceAsStream(String file) throws FileNotFoundException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        if(in == null) in = ClassLoader.getSystemClassLoader().getResourceAsStream(file);
        if(in == null) in = new FileInputStream(file);
        return in;
    }
    
}

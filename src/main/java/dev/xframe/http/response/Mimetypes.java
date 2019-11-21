package dev.xframe.http.response;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @see mime.types copy from nginx mime.types
 *
 */
public class Mimetypes {
    
    static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    
    static final Map<String, String> types = new HashMap<>();
    
    static {
        try {
            Properties properties = new Properties();
            properties.load(getResourceAsStream("mime.types"));
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
        return DEFAULT_MIME_TYPE;
    }
    
    static InputStream getResourceAsStream(String file) throws FileNotFoundException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        if(in == null) in = ClassLoader.getSystemClassLoader().getResourceAsStream(file);
        if(in == null) in = new FileInputStream(file);
        return in;
    }
    
}

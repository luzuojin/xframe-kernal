package dev.xframe.http.service.config;

/**
 * 
 * 文件处理(下载)
 * @author luzj
 *
 */
public interface FileHandler {
    
    /**
     * @param path
     * @return null if not exists
     */
    public String getPath(String path);

}

package dev.xframe.http.decode;

import java.net.URLDecoder;

import dev.xframe.tools.XStrings;

public class HttpURI extends QueryString {

    private String uri;
    private String path;
    
    public HttpURI(String uri) {
        super(uri, true);
        this.uri = uri;
    }
    
    /**
     * Returns the uri used to initialize this {@link URLDecoder}.
     */
    public String uri() {
        return uri;
    }

    /**
     * Returns the decoded path string of the URI.
     */
    public String path() {
        if (path == null) {
            int pathEndPos = uri.indexOf('?');
            path = decodeComponent(pathEndPos < 0 ? uri : uri.substring(0, pathEndPos), charset);
        }
        return path;
    }
    
    public String purePath() {
        return XStrings.trim(path(), '/');
    }

}

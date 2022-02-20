package dev.xframe.http.response;

public class ContentType {
	
    public static final ContentType TEXT = of("text/plain; charset=UTF-8");
    public static final ContentType HTML = of("text/html; charset=UTF-8");
    public static final ContentType JSON = of("application/json; charset=UTF-8");
    public static final ContentType FILE = of("application/octet-stream");
    public static final ContentType JSONP = of("application/javascript; charset=UTF-8");
    public static final ContentType BINARY = of("application/octet-stream");
    public static final ContentType FORCE_DOWNLOAD = of("application/force-download");
	
	public final String val;
	public ContentType(String val) {
		this.val = val;
	}
	
	public static ContentType of(String val) {
		return new ContentType(val);
	}
	
    public static ContentType mime(String file) {//filename is ok
    	return new ContentType(Mimetypes.get(file));
    }
    
}
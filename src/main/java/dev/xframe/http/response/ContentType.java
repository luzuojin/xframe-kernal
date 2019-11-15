package dev.xframe.http.response;

public class ContentType {
	
    public static final ContentType TEXT = new ContentType("text/plain; charset=UTF-8");
    public static final ContentType HTML = new ContentType("text/html; charset=UTF-8");
    public static final ContentType JSON = new ContentType("application/json; charset=UTF-8");
    public static final ContentType FILE = new ContentType("application/octet-stream");
    public static final ContentType JSONP = new ContentType("application/javascript; charset=UTF-8");
    public static final ContentType BINARY = new ContentType("application/octet-stream");
	
	final String val;
	public ContentType(String val) {
		this.val = val;
	}
    public String val() {
        return val;
    }
}
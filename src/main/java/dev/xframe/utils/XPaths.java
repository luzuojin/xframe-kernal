package dev.xframe.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

public class XPaths {
	
	public static File toFile(String path) {
		return new File(toPath(path));
	}
	
	public static File toFile(URL url) {
		return new File(toPath(url));
	}
	
	public static File toFile(URI uri) {
		return new File(toPath(uri));
	}
	
	public static String toPath(String path) {
		return utf8Decode(fromURIPath(path));
	}

	public static String toPath(URL url) {
		return utf8Decode(fromURIPath(url.getPath()));
	}
	
	public static String toPath(URI uri) {
		return utf8Decode(fromURIPath(uri.getPath()));
	}
	
	private static String utf8Decode(String path) {
		try {
			return URLDecoder.decode(path, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return path;
		}
	}
	
	private static String fromURIPath(String path) {
		String p = path;
		if (p.length() > 2 && p.endsWith("!/")) {//jar path
			// "/foo!/" --> "/foo"
			p = p.substring(0, p.length() - 2);
		}
		if ((p.length() > 2) && (p.charAt(2) == ':')) {//win path
			// "/c:/foo" --> "c:/foo"
			p = p.substring(1);
			// "c:/foo/" --> "c:/foo", but "c:/" --> "c:/"
			if ((p.length() > 3) && p.endsWith("/"))
				p = p.substring(0, p.length() - 1);
		} else if ((p.length() > 1) && p.endsWith("/")) {//unix path
			// "/foo/" --> "/foo"
			p = p.substring(0, p.length() - 1);
		}
		return p;
	}

}

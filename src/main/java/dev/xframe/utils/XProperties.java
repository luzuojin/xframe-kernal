package dev.xframe.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;

/**
 * 
 * composited properties
 * include System.properties
 * include xframe.properties
 * @author luzj
 */
public class XProperties {
	
	static final LinkedList<Properties> propsList = new LinkedList<>();
	static {
		propsList.addFirst(System.getProperties());
		//default properties file(xframe.properties or -Dxframe.properties.file)
		XProperties.load(get("xframe.properties.file", "xframe.properties"));
	}
	public static void addFirst(Properties props) {
		propsList.addFirst(props);
	}
	public static void addLast(Properties props) {
		propsList.addFirst(props);
	}
	public static void load(String propsFile) {
		InputStream in = XReflection.getResourceAsStream(propsFile);
		if(in != null) {
			try {
				Properties props = new Properties();
				props.load(in);
				addLast(props);
			} catch (IOException e) {//ignore
				XLogger.debug("load properties:", e);
			}
		}
	}

	public static String get(String key) {
		for (Properties props : propsList) {
			String val = props.getProperty(key);
			if(val != null) return val;
		}
		return null;
	}
	public static String get(String key, String def) {
		return XStrings.orElse(get(key), def);
	}
	
	public static int getAsInt(String key) {
		return getAsInt(key, -1);
	}
	public static int getAsInt(String key, int def) {
		return XStrings.orElse(get(key), def);
	}
	
	public static long getAsLong(String key) {
		return getAsLong(key, -1L);
	}
	public static long getAsLong(String key, long def) {
		return XStrings.orElse(get(key), def);
	}
	
	public static boolean getAsBool(String key) {
		return getAsBool(key, false);
	}
	public static boolean getAsBool(String key, boolean def) {
		return XStrings.orElse(get(key), def);
	}

}

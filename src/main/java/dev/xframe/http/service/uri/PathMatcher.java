package dev.xframe.http.service.uri;

import java.util.Map;
import java.util.regex.Matcher;

public class PathMatcher {
	
	public static final PathMatcher FALSE = new PathMatcher(false);
	public static final PathMatcher TRUE = new PathMatcher(true);
	
	private final boolean find;
	private final Matcher matcher;
	private final Map<String, Integer> groups;
	
	public PathMatcher(boolean find) {
		this(find, null, null);
	}
	public PathMatcher(boolean find, Matcher matcher, Map<String, Integer> groups) {
		this.find = find;
		this.matcher = matcher;
		this.groups = groups;
	}
	public boolean find(){
		return find;
	}
	/**
	 * @param group 从1开始 0为path本身
	 * @return
	 */
	public String group(int group) {
		if(find) {
			return matcher.group(group);
		}
		return null;
	}
	public String group(String key) {
		if(find) {
			Integer group = groups.get(key);
			if(group != null) {
				return matcher.group(group.intValue());
			}
		}
		throw new IllegalArgumentException("none path param ["+key+"] match");
	}
}
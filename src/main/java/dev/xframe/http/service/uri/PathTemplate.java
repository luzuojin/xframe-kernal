package dev.xframe.http.service.uri;

import java.util.HashMap;
import java.util.Map;

import dev.xframe.tools.XStrings;

public class PathTemplate {
	
	private String origin;
	private String mapping;
	private String regex;
	private Map<String, Integer> groups;
	
	public PathTemplate(String path) {
		origin = path;
		mapping = XStrings.trim(path, '/');
		if(isRegex(path)) {
			parse(mapping);
		}
	}
	
	public int group(String key) {
		return groups == null ? 0 : groups.get(key);
	}
	
	private void addGroup(String key, int group) {
		if(groups == null) groups = new HashMap<>();
		groups.put(key, group);
	}
	
	private boolean isRegex(String path) {
		return path.contains("{");
	}
	
	private void parse(String path) {
		StringBuilder regex = new StringBuilder();
		StringBuilder mapping = new StringBuilder();
		int group = 1;
		int st = -1, nd = -1;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if(c == '{') {
				st = i;
			}
			if(st == -1) {
				regex.append(c);
				mapping.append(c);
			}
			if(c == '}') {
				nd = i;
				
				regex.append("([\\w\\d_\\-\\\\.]+)");
				mapping.append(PathMap.WILDCARD);
				addGroup(path.substring(st+1, nd), group);
				
				st = nd = -1;
				++group;
			}
		}
		if(st != nd) throw new IllegalArgumentException("bad path: " + origin);
		
		this.regex = regex.toString();
		this.mapping = mapping.toString();
	}

	public String origin() {
		return origin;
	}

	public String mapping() {
		return mapping;
	}

	public String regex() {
		return regex;
	}

	public Map<String, Integer> groups() {
		return groups;
	}

}

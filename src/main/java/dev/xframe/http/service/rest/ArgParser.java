package dev.xframe.http.service.rest;

import dev.xframe.http.service.Request;
import dev.xframe.http.service.uri.PathMatcher;

public interface ArgParser {
	
	public Object parse(Request req, PathMatcher matcher);

}

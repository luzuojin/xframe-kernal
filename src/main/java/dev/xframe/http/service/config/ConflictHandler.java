package dev.xframe.http.service.config;

import dev.xframe.http.service.Service;

public interface ConflictHandler {
	
	public void handle(String path, Service s1, Service s2);

}

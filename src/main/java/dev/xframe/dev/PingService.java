package dev.xframe.dev;

import dev.xframe.http.Response;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.rest.HttpMethods;

@Rest("dev/ping")
public class PingService {
	
	@HttpMethods.GET
	public Object ping() {
		return Response.of("pong");
	}

}

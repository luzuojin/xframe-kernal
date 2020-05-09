package dev.xframe.dev;

import dev.xframe.http.Response;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.rest.HttpArgs;
import dev.xframe.http.service.rest.HttpMethods;
import dev.xframe.metric.Metrics;

@Rest("dev/metrics")
public class MetricsService {

	@HttpMethods.GET
	public Object metrics() {
        StringBuilder sb = new StringBuilder(Metrics.columns()).append('\n');
        Metrics.metrics().forEach((k, v)->sb.append(v.toString()).append('\n'));
        return Response.of(sb.toString());
	}
	
	@HttpMethods.GET("watch")
	public Object watch(@HttpArgs.Param boolean w) {
		if(w) {
			Metrics.watch();
		} else {
			Metrics.unwatch();
		}
		return Response.of("succ");
	}
	
}

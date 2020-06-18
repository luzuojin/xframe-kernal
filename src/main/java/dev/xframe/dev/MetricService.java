package dev.xframe.dev;

import dev.xframe.http.Response;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.rest.HttpArgs;
import dev.xframe.http.service.rest.HttpMethods;
import dev.xframe.metric.Metric;

@Rest("dev/metric")
public class MetricService {

	@HttpMethods.GET
	public Object metric() {
        return Response.of(Metric.console());
	}
	
	@HttpMethods.GET("watch")
	public Object watch(@HttpArgs.Param boolean w) {
		if(w) {
			Metric.watch();
		} else {
			Metric.unwatch();
		}
		return Response.of("succ");
	}
	
}

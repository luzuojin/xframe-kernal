package dev.xframe.dev;

import dev.xframe.http.Response;
import dev.xframe.http.service.Rest;
import dev.xframe.http.service.rest.HttpMethods;
import dev.xframe.metric.Metric;

@Rest("dev/metric")
public class MetricService {

	@HttpMethods.GET
	public Object metric() {
        return Response.of(Metric.console());
	}
	
	@HttpMethods.GET("open")
	public Object open() {
		Metric.open();
		return Response.of("succ");
	}
	
	@HttpMethods.GET("close")
	public Object close() {
		Metric.close();
		return Response.of("succ");
	}
	
	@HttpMethods.GET("clean")
	public Object clean() {
		Metric.clean();
		return Response.of("succ");
	}
	
}

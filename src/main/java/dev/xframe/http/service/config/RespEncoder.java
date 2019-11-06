package dev.xframe.http.service.config;

import dev.xframe.http.service.Response;

@FunctionalInterface
public interface RespEncoder {

	Response encode(Object resp);

}

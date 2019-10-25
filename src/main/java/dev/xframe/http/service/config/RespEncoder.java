package dev.xframe.http.service.config;

import dev.xframe.http.service.Response;

public interface RespEncoder {

	Response encode(Object resp);

}

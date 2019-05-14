package dev.xframe.http.service.rest;

import dev.xframe.http.service.Response;

public interface RespEncoder {

	Response encode(Object resp);

}

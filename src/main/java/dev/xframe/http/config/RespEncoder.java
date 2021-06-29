package dev.xframe.http.config;

import dev.xframe.http.Response;

@FunctionalInterface
public interface RespEncoder {

    Response encode(Object resp);

}

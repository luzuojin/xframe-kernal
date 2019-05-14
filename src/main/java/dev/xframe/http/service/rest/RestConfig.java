package dev.xframe.http.service.rest;

import dev.xframe.http.service.ServiceConfig;

public interface RestConfig extends ServiceConfig {

    RespEncoder getRespEncoder();

    BodyDecoder getBodyDecoder();

}

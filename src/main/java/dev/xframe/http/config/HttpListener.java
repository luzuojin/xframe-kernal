package dev.xframe.http.config;

import dev.xframe.http.Request;
import dev.xframe.http.Response;
import dev.xframe.inject.Composite;

@Composite
public interface HttpListener {
    default void onAccessStarting(Request req) {
    }
    default void onAccessComplete(Request req, Response resp) {
    }
    default void onServiceNotFound(Request req) {
    }
    default void onExceptionCaught(Request req, Throwable ex) {
    }
}

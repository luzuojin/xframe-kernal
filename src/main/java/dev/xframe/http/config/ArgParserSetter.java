package dev.xframe.http.config;

import java.util.function.Function;

public interface ArgParserSetter {

    <T> void accept(Class<T> cls, Function<String, T> func);

}

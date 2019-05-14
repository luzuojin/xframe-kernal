package dev.xframe.net.cmd;

public interface CommandBuilder {
    Command build(Class<?> clazz);
}

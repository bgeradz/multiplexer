package com.bgeradz.multiplexer;

public interface Configurator<T> {
    void validate() throws ConfigException;
    T build();
}

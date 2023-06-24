package com.smallc.tinyrpc.network.transport;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.transport
 */
public interface TransportServer {

    void start(String host, int port) throws Exception;

    void stop();

}

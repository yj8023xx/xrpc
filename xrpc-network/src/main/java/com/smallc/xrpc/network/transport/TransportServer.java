package com.smallc.xrpc.network.transport;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport
 */
public interface TransportServer {

    void start(String host, int port, RequestHandlerRegistry requestHandlerRegistry) throws Exception;

    void stop();

}

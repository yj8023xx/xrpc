package com.smallc.xrpc.network.transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport
 */
public interface Transport<T> {
    
    /**
     * send request
     * @param request rpc request
     * @return future
     */
    CompletableFuture<T> send(T request);

}

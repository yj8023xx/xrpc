package com.smallc.xrpc.network.transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport
 */
public class ResponseFuture<T> {

    private final long requestId;
    private final CompletableFuture<T> future;
    private final long timestamp;

    public ResponseFuture(long requestId, CompletableFuture<T> future) {
        this.requestId = requestId;
        this.future = future;
        timestamp = System.nanoTime();
    }

    public long getRequestId() {
        return requestId;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public long getTimestamp() {
        return timestamp;
    }

}

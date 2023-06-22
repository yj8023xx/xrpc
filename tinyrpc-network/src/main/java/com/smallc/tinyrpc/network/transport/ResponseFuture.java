package com.smallc.tinyrpc.network.transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.transport
 */
public class ResponseFuture<T> {

    private final int requestId;
    private final CompletableFuture<T> future;
    private final long timestamp;

    public ResponseFuture(int requestId, CompletableFuture<T> future) {
        this.requestId = requestId;
        this.future = future;
        timestamp = System.nanoTime();
    }

    public int getRequestId() {
        return requestId;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public long getTimestamp() {
        return timestamp;
    }

}

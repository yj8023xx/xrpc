package com.smallc.xrpc.network.transport.rdma;

import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.PendingRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import com.smallc.xrpc.network.transport.Transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/2
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.rdma
 */
public class RdmaTransport implements Transport<XRpcMessage> {

    private final XRpcClientEndpoint endPoint;
    private final PendingRequests<XRpcMessage> pendingRequests;

    public RdmaTransport(XRpcClientEndpoint endPoint, PendingRequests<XRpcMessage> pendingRequests) {
        this.endPoint = endPoint;
        this.endPoint.setPendingRequests(pendingRequests);
        this.pendingRequests = pendingRequests;
    }

    @Override
    public CompletableFuture<XRpcMessage> send(XRpcMessage message) {
        // Build return value
        CompletableFuture<XRpcMessage> completableFuture = new CompletableFuture<>();
        try {
            pendingRequests.put(new ResponseFuture(message.getHeader().getRequestId(), completableFuture));
            endPoint.send(message);
        } catch (Throwable t) {
            pendingRequests.remove(message.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }

}

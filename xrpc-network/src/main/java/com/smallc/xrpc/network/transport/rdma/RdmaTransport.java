package com.smallc.xrpc.network.transport.rdma;

import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.InFlightRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import com.smallc.xrpc.network.transport.Transport;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/2
 * @since com.smallc.xrpc.network.transport.rdma
 */
public class RdmaTransport implements Transport<XRpcMessage> {

    private final XRpcClientEndpoint endPoint;
    private final InFlightRequests<XRpcMessage> inFlightRequests;

    public RdmaTransport(XRpcClientEndpoint endPoint, InFlightRequests<XRpcMessage> inFlightRequests) {
        this.endPoint = endPoint;
        this.inFlightRequests = inFlightRequests;
    }

    @Override
    public CompletableFuture<XRpcMessage> send(XRpcMessage message) {
        // 构建返回值
        CompletableFuture<XRpcMessage> completableFuture = new CompletableFuture<>();
        try {
            // 将在途请求放到inFlightRequests中
            inFlightRequests.put(new ResponseFuture(message.getHeader().getRequestId(), completableFuture));
            // 发送数据
            endPoint.send(message);
        } catch (Throwable t) {
            // 处理发送异常
            inFlightRequests.remove(message.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }

}

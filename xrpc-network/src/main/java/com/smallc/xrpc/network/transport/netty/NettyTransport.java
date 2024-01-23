package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.PendingRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import com.smallc.xrpc.network.transport.Transport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.network.transport.netty
 */
public class NettyTransport implements Transport<XRpcMessage> {

    private final Channel channel;
    private final PendingRequests<XRpcMessage> pendingRequests;

    NettyTransport(Channel channel, PendingRequests<XRpcMessage> pendingRequests) {
        this.channel = channel;
        this.pendingRequests = pendingRequests;
    }

    // TODO: Is it Thread-safe?
    @Override
    public CompletableFuture<XRpcMessage> send(XRpcMessage message) {
        // Build the return value
        CompletableFuture<XRpcMessage> completableFuture = new CompletableFuture<>();
        try {
            // Put the in-flight request into the pendingRequests
            pendingRequests.put(new ResponseFuture(message.getHeader().getRequestId(), completableFuture));
            // Send the command
            channel.writeAndFlush(message).addListener((ChannelFutureListener) channelFuture -> {
                // Handle the case of sending failure
                if (!channelFuture.isSuccess()) {
                    completableFuture.completeExceptionally(channelFuture.cause());
                    channel.close();
                }
            });
        } catch (Throwable t) {
            // Handle the sending exception
            pendingRequests.remove(message.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }
}


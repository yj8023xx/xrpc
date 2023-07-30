package com.smallc.xrpc.network.transport.netty;

import com.smallc.xrpc.network.protocol.XRpcMessage;
import com.smallc.xrpc.network.transport.InFlightRequests;
import com.smallc.xrpc.network.transport.ResponseFuture;
import com.smallc.xrpc.network.transport.Transport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.CompletableFuture;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.transport.netty
 */
public class NettyTransport implements Transport<XRpcMessage> {

    private final Channel channel;
    private final InFlightRequests<XRpcMessage> inFlightRequests;

    NettyTransport(Channel channel, InFlightRequests<XRpcMessage> inFlightRequests) {
        this.channel = channel;
        this.inFlightRequests = inFlightRequests;
    }

    // TODO whether it is Thread safety?
    @Override
    public CompletableFuture<XRpcMessage> send(XRpcMessage message) {
        // 构建返回值
        CompletableFuture<XRpcMessage> completableFuture = new CompletableFuture<>();
        try {
            // 将在途请求放到inFlightRequests中
            inFlightRequests.put(new ResponseFuture(message.getHeader().getRequestId(), completableFuture));
            // 发送命令
            channel.writeAndFlush(message).addListener((ChannelFutureListener) channelFuture -> {
                // 处理发送失败的情况
                if (!channelFuture.isSuccess()) {
                    completableFuture.completeExceptionally(channelFuture.cause());
                    channel.close();
                }
            });
        } catch (Throwable t) {
            // 处理发送异常
            inFlightRequests.remove(message.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }

}

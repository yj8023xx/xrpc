package com.smallc.tinyrpc.network.transport.netty;

import com.smallc.tinyrpc.common.bean.RpcRequest;
import com.smallc.tinyrpc.common.bean.RpcRequestId;
import com.smallc.tinyrpc.common.serializer.ProtostuffSerializerUtil;
import com.smallc.tinyrpc.network.protocol.*;
import com.smallc.tinyrpc.network.transport.InFlightRequests;
import com.smallc.tinyrpc.network.transport.ResponseFuture;
import com.smallc.tinyrpc.network.transport.Transport;
import com.smallc.tinyrpc.common.serializer.SerializeType;
import com.smallc.tinyrpc.common.serializer.SerializeUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.network.transport.netty
 */
public class NettyTransport implements Transport<TinyRpcPacket> {

    private final Channel channel;
    private final InFlightRequests<TinyRpcPacket> inFlightRequests;

    NettyTransport(Channel channel, InFlightRequests<TinyRpcPacket> inFlightRequests) {
        this.channel = channel;
        this.inFlightRequests = inFlightRequests;
    }

    @Override
    public CompletableFuture<TinyRpcPacket> send(TinyRpcPacket data) {
        // 构建返回值
        CompletableFuture<TinyRpcPacket> completableFuture = new CompletableFuture<>();
        try {
            // 将在途请求放到inFlightRequests中
            inFlightRequests.put(new ResponseFuture(data.getHeader().getRequestId(), completableFuture));
            // 发送命令
            channel.writeAndFlush(data).addListener((ChannelFutureListener) channelFuture -> {
                // 处理发送失败的情况
                if (!channelFuture.isSuccess()) {
                    completableFuture.completeExceptionally(channelFuture.cause());
                    channel.close();
                }
            });
        } catch (Throwable t) {
            // 处理发送异常
            inFlightRequests.remove(data.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;
    }

}

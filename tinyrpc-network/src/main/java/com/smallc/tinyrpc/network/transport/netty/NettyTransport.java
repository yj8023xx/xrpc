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
 * @author LiYue
 * Date: 2019/9/20
 */
public class NettyTransport implements Transport<TinyRpcPacket> {

    private final Channel channel;
    private final InFlightRequests<TinyRpcPacket> inFlightRequests;

    private static int serializeMethod = SerializeType.HESSIAN.getVal();

    private static int version = 1;

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

    public static byte[] invokeRemote(Transport<TinyRpcPacket> transport, RpcRequest request) {
        TinyRpcHeader header = new TinyRpcHeader(TinyRpcMessageType.RPC_REQUEST.getVal(), serializeMethod, RpcRequestId.next());
        byte[] payload = ProtostuffSerializerUtil.serialize(request);
        TinyRpcPacket packet = new TinyRpcPacket(header, payload);
        try {
            TinyRpcPacket response = transport.send(packet).get();
            TinyRpcResponseHeader responseHeader = (TinyRpcResponseHeader) response.getHeader();
            if (responseHeader.getCode() == TinyRpcCode.SUCCESS.getVal()) {
                return response.getPayload();
            } else {
                throw new Exception(responseHeader.getError());
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

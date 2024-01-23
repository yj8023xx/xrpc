package com.smallc.xrpc.client.stub;

import com.smallc.xrpc.common.serializer.Serializer;
import com.smallc.xrpc.common.utils.RpcRequestId;
import com.smallc.xrpc.network.protocol.*;
import com.smallc.xrpc.network.protocol.XRpcRequest;
import com.smallc.xrpc.network.transport.Transport;

import java.util.concurrent.ExecutionException;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.stub
 */
public interface ServiceStub {

    default byte[] invokeRemote(Transport<XRpcMessage> transport, Serializer serializer, XRpcRequest request) {
        XRpcHeader header = new XRpcHeader((byte) XRpcMessageType.RPC_REQUEST.getValue(), (byte) serializer.type().getValue(), RpcRequestId.next(), (byte) XRpcStatus.SUCCESS.getValue());
        byte[] payload = serializer.serialize(request);
        XRpcMessage message = new XRpcMessage(header, payload);
        try {
            XRpcMessage response = transport.send(message).get();
            XRpcHeader responseHeader = response.getHeader();
            if (responseHeader.getStatus() == XRpcStatus.SUCCESS.getValue()) {
                return response.getPayload();
            } else {
                throw new Exception(XRpcStatus.getContent(responseHeader.getStatus()));
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}

package com.smallc.xrpc.client.stub;

import com.smallc.xrpc.common.serializer.Serializer;
import com.smallc.xrpc.common.utils.RpcRequestId;
import com.smallc.xrpc.network.protocol.*;
import com.smallc.xrpc.network.request.RpcRequest;
import com.smallc.xrpc.network.transport.Transport;

import java.util.concurrent.ExecutionException;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.stub
 */
public interface ServiceStub {

    default byte[] invokeRemote(Transport<XRpcMessage> transport, Serializer serializer, RpcRequest request) {
        XRpcHeader header = new XRpcHeader(XRpcMessageType.RPC_REQUEST.getValue(), serializer.type().getValue(), RpcRequestId.next());
        byte[] payload = serializer.serialize(request);
        XRpcMessage message = new XRpcMessage(header, payload);
        try {
            XRpcMessage response = transport.send(message).get();
            XRpcResponseHeader responseHeader = (XRpcResponseHeader) response.getHeader();
            if (responseHeader.getCode() == XRpcCode.SUCCESS.getValue()) {
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

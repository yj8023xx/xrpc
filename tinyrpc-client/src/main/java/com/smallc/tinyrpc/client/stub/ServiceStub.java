package com.smallc.tinyrpc.client.stub;

import com.smallc.tinyrpc.common.bean.RpcRequest;
import com.smallc.tinyrpc.common.bean.RpcRequestId;
import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.serializer.ProtostuffSerializerUtil;
import com.smallc.tinyrpc.common.serializer.SerializeType;
import com.smallc.tinyrpc.common.serializer.Serializer;
import com.smallc.tinyrpc.network.protocol.*;
import com.smallc.tinyrpc.network.transport.Transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.stub
 */
public class ServiceStub implements InvocationHandler {

    private List<Transport> transports;
    private LoadBalancer loadBalancer;
    private Serializer serializer;

    public ServiceStub(List<Transport> transports, LoadBalancer loadBalancer, Serializer serializer) {
        this.transports = transports;
        this.loadBalancer = loadBalancer;
        this.serializer = serializer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String interfaceName = method.getDeclaringClass().getCanonicalName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        RpcRequest request = new RpcRequest(interfaceName, methodName, parameterTypes, args);
        byte[] response = invokeRemote(loadBalancer.select(transports), request);
        return serializer.deserialize(response, method.getReturnType());
    }

    public byte[] invokeRemote(Transport<TinyRpcPacket> transport, RpcRequest request) {
        TinyRpcHeader header = new TinyRpcHeader(TinyRpcMessageType.RPC_REQUEST.getCode(), SerializeType.codeOf(serializer.type()), RpcRequestId.next());
        byte[] payload = serializer.serialize(request);
        TinyRpcPacket packet = new TinyRpcPacket(header, payload);
        try {
            TinyRpcPacket response = transport.send(packet).get();
            TinyRpcResponseHeader responseHeader = (TinyRpcResponseHeader) response.getHeader();
            if (responseHeader.getCode() == TinyRpcCode.SUCCESS.getCode()) {
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

package com.smallc.tinyrpc.client.stub;

import com.smallc.tinyrpc.common.bean.RpcRequest;;
import com.smallc.tinyrpc.common.serializer.ProtostuffSerializerUtil;
import com.smallc.tinyrpc.network.transport.Transport;
import com.smallc.tinyrpc.network.transport.netty.NettyTransport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.stub
 */
public class ServiceStub implements InvocationHandler {

    private Transport transport;

    public ServiceStub(Transport transport) {
        this.transport = transport;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String interfaceName = method.getDeclaringClass().getCanonicalName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        RpcRequest request = new RpcRequest(interfaceName, methodName, parameterTypes, args);
        byte[] response = NettyTransport.invokeRemote(transport, request);
        return ProtostuffSerializerUtil.deserialize(response, method.getReturnType());
    }

}

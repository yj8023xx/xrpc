package com.smallc.tinyrpc.client.stub.impl;

import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.serializer.SerializeType;
import com.smallc.tinyrpc.common.serializer.Serializer;
import com.smallc.tinyrpc.network.transport.Transport;
import com.smallc.tinyrpc.client.stub.ServiceStub;
import com.smallc.tinyrpc.client.stub.StubFactory;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.stub
 */
public class ProxyStubFactory implements StubFactory {

    @Override
    public <T> T createStub(Class<T> serviceClass, List<Transport> transports, LoadBalancer loadBalancer, Serializer serializer) {
        T stub = (T) Proxy.newProxyInstance(ProxyStubFactory.class.getClassLoader(), new Class[]{serviceClass}, new ServiceStub(transports, loadBalancer, serializer));
        return stub;
    }

}

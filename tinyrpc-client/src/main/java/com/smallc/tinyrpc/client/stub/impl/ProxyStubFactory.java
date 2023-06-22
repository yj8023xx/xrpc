package com.smallc.tinyrpc.client.stub.impl;

import com.smallc.tinyrpc.network.transport.Transport;
import com.smallc.tinyrpc.client.stub.ServiceStub;
import com.smallc.tinyrpc.client.stub.StubFactory;

import java.lang.reflect.Proxy;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.stub
 */
public class ProxyStubFactory implements StubFactory {

    @Override
    public <T> T createStub(Transport transport, Class<T> serviceClass) {
        T stub = (T) Proxy.newProxyInstance(ProxyStubFactory.class.getClassLoader(), new Class[]{serviceClass}, new ServiceStub(transport));
        return stub;
    }

}

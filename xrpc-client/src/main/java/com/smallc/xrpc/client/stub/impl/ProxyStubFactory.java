package com.smallc.xrpc.client.stub.impl;

import com.smallc.xrpc.client.XRpcClient;
import com.smallc.xrpc.client.stub.ServiceStub;
import com.smallc.xrpc.client.stub.StubFactory;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;
import com.smallc.xrpc.common.serializer.Serializer;
import com.smallc.xrpc.network.protocol.XRpcRequest;
import com.smallc.xrpc.network.transport.Transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.stub
 */
public class ProxyStubFactory implements StubFactory {

    class ProxyStub implements ServiceStub, InvocationHandler {

        private XRpcClient client;
        private LoadBalancer<Transport> loadBalancer;
        private Serializer serializer;

        public ProxyStub(XRpcClient client, LoadBalancer loadBalancer, Serializer serializer) {
            this.client = client;
            this.loadBalancer = loadBalancer;
            this.serializer = serializer;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String serviceName = method.getDeclaringClass().getCanonicalName();
            String methodName = method.getName();
            HashMap<String, Object> argMap = new LinkedHashMap<>();
            Parameter[] params = method.getParameters();
            for (int i = 0; i < args.length; i++) {
                argMap.put(params[i].getName(), args[i]);
            }
            XRpcRequest request = new XRpcRequest(serviceName, methodName, argMap);
            byte[] response = invokeRemote(loadBalancer.select(), serializer, request);
            return serializer.deserialize(response, method.getReturnType());
        }
    }

    @Override
    public <T> T createStub(Class<T> serviceClass, XRpcClient client, LoadBalancer loadBalancer, Serializer serializer) {
        T stub = (T) Proxy.newProxyInstance(ProxyStubFactory.class.getClassLoader(), new Class[]{serviceClass}, new ProxyStub(client, loadBalancer, serializer));
        return stub;
    }

    @Override
    public <T> T createAsyncStub(Class<T> serviceClass, XRpcClient client, LoadBalancer loadBalancer, Serializer serializer) {
        return null;
    }

}

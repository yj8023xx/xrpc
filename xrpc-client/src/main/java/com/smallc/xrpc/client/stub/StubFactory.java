package com.smallc.xrpc.client.stub;

import com.smallc.xrpc.client.XRpcClient;
import com.smallc.xrpc.common.loadbalancer.LoadBalancer;
import com.smallc.xrpc.common.serializer.Serializer;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.server.transport.TransportServer.stub
 */
public interface StubFactory {

    <T> T createStub(Class<T> serviceClass, XRpcClient client, LoadBalancer loadBalancer, Serializer serializer);

    <T> T createAsyncStub(Class<T> serviceClass, XRpcClient client, LoadBalancer loadBalancer, Serializer serializer);

}

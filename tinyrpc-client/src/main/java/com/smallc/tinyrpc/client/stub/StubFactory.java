package com.smallc.tinyrpc.client.stub;

import com.smallc.tinyrpc.common.loadbalancer.LoadBalancer;
import com.smallc.tinyrpc.common.serializer.SerializeType;
import com.smallc.tinyrpc.common.serializer.Serializer;
import com.smallc.tinyrpc.network.transport.Transport;

import java.util.List;

/**
 * @author LiYue
 * Date: 2019/9/27
 */
public interface StubFactory {
    <T> T createStub(Class<T> serviceClass, List<Transport> transports, LoadBalancer loadBalancer, Serializer serializer);
}

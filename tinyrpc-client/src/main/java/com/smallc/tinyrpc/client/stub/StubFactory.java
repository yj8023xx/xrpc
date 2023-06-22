package com.smallc.tinyrpc.client.stub;

import com.smallc.tinyrpc.network.transport.Transport;

/**
 * @author LiYue
 * Date: 2019/9/27
 */
public interface StubFactory {
    <T> T createStub(Transport transport, Class<T> serviceClass);
}

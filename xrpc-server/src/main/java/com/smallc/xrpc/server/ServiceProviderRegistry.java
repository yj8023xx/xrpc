package com.smallc.xrpc.server;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.network.transport.TransportServer.server
 */
public interface ServiceProviderRegistry {

    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);

    <T> void removeServiceProvider(Class<? extends T> serviceClass);

}

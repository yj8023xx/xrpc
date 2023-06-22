package com.smallc.tinyrpc.server;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.server
 */
public interface ServiceProviderRegistry {

    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);

    <T> void removeServiceProvider(Class<? extends T> serviceClass);

}

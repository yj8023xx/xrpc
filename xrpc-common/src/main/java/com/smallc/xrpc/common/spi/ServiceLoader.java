package com.smallc.xrpc.common.spi;

import com.smallc.xrpc.common.annotation.RpcSingleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * SPI类加载器帮助类
 *
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.common.spi
 */
public class ServiceLoader {
    private final static Map<String, Object> singletonServices = new HashMap<>();

    public synchronized static <S> S load(Class<S> service) {
        return StreamSupport.
                stream(java.util.ServiceLoader.load(service).spliterator(), false)
                .map(ServiceLoader::singletonFilter)
                .findFirst().orElseThrow(ServiceLoadException::new);
    }

    public synchronized static <S> Collection<S> loadAll(Class<S> service) {
        return StreamSupport.
                stream(java.util.ServiceLoader.load(service).spliterator(), false)
                .map(ServiceLoader::singletonFilter).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <S> S singletonFilter(S service) {
        if (service.getClass().isAnnotationPresent(RpcSingleton.class)) {
            String className = service.getClass().getCanonicalName();
            Object singletonInstance = singletonServices.putIfAbsent(className, service);
            return singletonInstance == null ? service : (S) singletonInstance;
        } else {
            return service;
        }
    }

}

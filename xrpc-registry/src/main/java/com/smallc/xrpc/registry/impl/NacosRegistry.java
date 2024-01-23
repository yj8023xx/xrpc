package com.smallc.xrpc.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.smallc.xrpc.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/6/27
 * @since com.smallc.xrpc.registry.impl
 */
public class NacosRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(NacosRegistry.class);

    private static final Collection<String> schemes = Collections.singleton("nacos");

    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    private NamingService namingService = null;

    @Override
    public void connect(URI registryUri) {
        if (null == namingService) {
            try {
                namingService = NamingFactory.createNamingService(registryUri.getHost() + ":" + registryUri.getPort());
            } catch (NacosException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void registerService(String serviceName, URI uri) throws Exception {
        Instance instance = new Instance();
        instance.setIp(uri.getHost());
        instance.setPort(uri.getPort());
        instance.setServiceName(serviceName);
        namingService.registerInstance(serviceName, instance);
    }

    @Override
    public List<URI> getServiceAddress(String serviceName) throws Exception {
        List<Instance> instances = namingService.getAllInstances(serviceName);
        List<URI> uris = new ArrayList<>();
        for (Instance instance : instances) {
            uris.add(URI.create("rpc://" + instance.getIp() + ":" + instance.getPort()));
        }
        return uris;
    }

    @Override
    public Map<String, List<URI>> broadcastServiceAddress() {
        return null;
    }

}

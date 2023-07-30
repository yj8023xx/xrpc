package com.smallc.xrpc.registry.impl;

import com.smallc.xrpc.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Override
    public void connect(URI nameServiceUri) {

    }

    @Override
    public void registerService(String serviceName, URI uri) throws Exception {

    }

    @Override
    public List<URI> getServiceAddress(String serviceName) throws Exception {
        return null;
    }

    @Override
    public Map<String, List<URI>> broadcastServiceAddress() {
        return null;
    }

}

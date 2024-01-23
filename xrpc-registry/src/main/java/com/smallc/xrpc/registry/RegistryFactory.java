package com.smallc.xrpc.registry;

import com.smallc.xrpc.registry.impl.NacosRegistry;
import com.smallc.xrpc.registry.impl.ZooKeeperRegistry;

import java.net.URI;

/**
 * @author yj8023xx
 * @version 1.0
 * @date 2023/7/8
 * @since com.smallc.xrpc.registry
 */
public class RegistryFactory {

    /**
     * Get a registry according to uri
     *
     * @param registryUri
     * @return registry
     */
    public static Registry getRegistry(URI registryUri) {
        Registry registry = null;
        switch (registryUri.getScheme()) {
            case "zookeeper":
                registry = new ZooKeeperRegistry();
                break;
            case "nacos":
                registry = new NacosRegistry();
                break;
        }
        if (registry != null) {
            registry.connect(registryUri);
        }
        return registry;
    }

}

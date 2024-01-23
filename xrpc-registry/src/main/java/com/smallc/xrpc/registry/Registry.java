package com.smallc.xrpc.registry;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Registry
 *
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.registry
 */
public interface Registry {

    /**
     * Get all supported schemes
     */
    Collection<String> supportedSchemes();

    /**
     * Connect to registry
     *
     * @param registryUri
     */
    void connect(URI registryUri);

    /**
     * Register service
     *
     * @param serviceName
     * @param uri
     */
    void registerService(String serviceName, URI uri) throws Exception;

    /**
     * Get service address
     *
     * @param serviceName
     * @return A list of service address
     */
    List<URI> getServiceAddress(String serviceName) throws Exception;

    Map<String, List<URI>> broadcastServiceAddress();

}

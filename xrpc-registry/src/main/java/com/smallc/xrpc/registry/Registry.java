package com.smallc.xrpc.registry;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 注册中心
 *
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.registry
 */
public interface Registry {

    /**
     * 所有支持的协议
     */
    Collection<String> supportedSchemes();

    /**
     * 连接注册中心
     *
     * @param nameServiceUri 注册中心地址
     */
    void connect(URI nameServiceUri);

    /**
     * 注册服务
     *
     * @param serviceName 服务名称
     * @param uri         服务地址
     */
    void registerService(String serviceName, URI uri) throws Exception;

    /**
     * 获取服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    List<URI> getServiceAddress(String serviceName) throws Exception;

    Map<String, List<URI>> broadcastServiceAddress();

}

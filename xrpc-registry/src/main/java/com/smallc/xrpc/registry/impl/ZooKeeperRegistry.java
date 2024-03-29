package com.smallc.xrpc.registry.impl;

import com.smallc.xrpc.registry.Registry;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author yj8023xx
 * @version 1.0
 * @since com.smallc.xrpc.registry.impl
 */
public class ZooKeeperRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperRegistry.class);
    private static final int ZK_SESSION_TIMEOUT = 5000;
    private static final int ZK_CONNECTION_TIMEOUT = 1000;
    private static final String ZK_REGISTRY_PATH = "/registry";
    private static final Collection<String> schemes = Collections.singleton("zookeeper");
    private ZkClient zkClient;

    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public void connect(URI nameServiceUri) {
        // 创建 ZooKeeper 客户端
        // ZKServers: ip:port
        this.zkClient = new ZkClient(nameServiceUri.getHost() + ":" + nameServiceUri.getPort(), ZK_SESSION_TIMEOUT, ZK_CONNECTION_TIMEOUT);
        this.zkClient.setZkSerializer(new MyZkSerializer());
    }

    @Override
    public void registerService(String serviceName, URI uri) throws Exception {
        // 创建 registry 节点（持久）
        String registryPath = ZK_REGISTRY_PATH;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
        }
        // 创建 service 节点（持久）
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
        }
        // 创建 address 节点（临时）
        String addressPath = servicePath + "/";
        zkClient.createEphemeralSequential(addressPath, uri.toString());
    }

    @Override
    public List<URI> getServiceAddress(String serviceName) throws Exception {
        // 获取 service 节点
        String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            throw new RuntimeException(String.format("Can not find any service node on path: %s", servicePath));
        }
        List<String> addressList = zkClient.getChildren(servicePath);
        if (CollectionUtils.isEmpty(addressList)) {
            throw new RuntimeException(String.format("Can not find any address node on path: %s", servicePath));
        }
        List<URI> uris = new ArrayList<>();
        for (String address : addressList) {
            // 获取 address 节点的值
            String addressPath = servicePath + "/" + address;
            String uri = zkClient.readData(addressPath);
            uris.add(URI.create(uri));
        }
        return uris;
    }

    @Override
    public Map<String, List<URI>> broadcastServiceAddress() {
        return null;
    }

    private class MyZkSerializer implements ZkSerializer {
        public Object deserialize(byte[] bytes) throws ZkMarshallingError {
            return new String(bytes, Charset.forName("UTF-8"));
        }

        public byte[] serialize(Object obj) throws ZkMarshallingError {
            return String.valueOf(obj).getBytes(Charset.forName("UTF-8"));
        }
    }

}

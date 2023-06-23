package com.smallc.tinyrpc.registry.impl;

import com.smallc.tinyrpc.registry.NameService;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.tinyrpc.registry.impl
 */
public class ZooKeeperNameService implements NameService {

    private static final Logger logger = LoggerFactory.getLogger(JdbcNameService.class);
    private static final Collection<String> schemes = Collections.singleton("zookeeper");

    private ZkClient zkClient;

    private static final int ZK_SESSION_TIMEOUT = 5000;
    private static final int ZK_CONNECTION_TIMEOUT = 1000;
    private static final String ZK_REGISTRY_PATH = "/registry";

    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public void connect(URI nameServiceUri) {
        // 创建 ZooKeeper 客户端
        // ZKServers: ip:port
        this.zkClient = new ZkClient(nameServiceUri.getHost() + ":" + nameServiceUri.getPort(), ZK_SESSION_TIMEOUT, ZK_CONNECTION_TIMEOUT);
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
        String addressNode = zkClient.createEphemeralSequential(addressPath, uri);
    }

    @Override
    public URI getServiceAddress(String serviceName) throws Exception {
        // 获取 service 节点
        String servicePath = ZK_REGISTRY_PATH + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
        }
        List<String> addressList = zkClient.getChildren(servicePath);
        if (CollectionUtils.isEmpty(addressList)) {
            throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
        }
        // 获取 address 节点
        String address;
        int size = addressList.size();
        if (size == 1) {
            // 若只有一个地址，则获取该地址
            address = addressList.get(0);
        } else {
            // 若存在多个地址，则随机获取一个地址
            address = addressList.get(ThreadLocalRandom.current().nextInt(size));
        }
        // 获取 address 节点的值
        String addressPath = servicePath + "/" + address;
        return zkClient.readData(addressPath);
    }

}
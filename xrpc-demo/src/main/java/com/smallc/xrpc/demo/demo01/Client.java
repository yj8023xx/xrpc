package com.smallc.xrpc.demo.demo01;

import com.smallc.xrpc.client.XRpcClient;
import com.smallc.xrpc.api.hello.HelloService;
import com.smallc.xrpc.common.serializer.SerializationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author zhang
 * @version 1.0
 * @since com.smallc.xrpc.demo.demo01
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 2181;
        URI nameServiceUri = URI.create("zookeeper://" + host + ":" + port);

        logger.info("创建xRPC客户端实例...");
        XRpcClient client = new XRpcClient(nameServiceUri);

        logger.info("创建服务桩...");
        HelloService helloService = client.getRemoteService(HelloService.class, SerializationType.JSON);
        assert helloService != null;

        String name = "World!";
        logger.info("请求服务, name: {}...", name);
        String response = helloService.hello(name);
        logger.info("收到响应: {}.", response);
    }

}
